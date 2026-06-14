# Smoke test script for JourneyPlus APIs
# Usage: Start the application (jar or mvn spring-boot:run), then run in PowerShell:
#   powershell -ExecutionPolicy Bypass -File .\tools\smoke_test.ps1

$base = 'http://localhost:8090'
$results = @()

$tests = @(
    @{ method='GET';  path='/api/policies'; desc='Get all policies' },
    @{ method='GET';  path='/api/policies/city-tiers'; desc='Get city tiers' },
    @{ method='GET';  path='/api/policies/role/EMPLOYEE'; desc='Get policy by role' },

    @{ method='POST'; path='/api/auth/register'; desc='Auth register (dummy)'; body=@{ username='smoketest_user'; password='TestPass123!'; email='smoke@example.com'; role='EMPLOYEE' } },
    @{ method='POST'; path='/api/auth/login'; desc='Auth login (dummy)'; body=@{ username='nonexistent'; password='bad' } },
    @{ method='POST'; path='/api/auth/refresh'; desc='Auth refresh (dummy)'; body=@{ refreshToken='x' } },

    @{ method='GET';  path='/api/users/me'; desc='Current user (requires auth)' },
    @{ method='GET';  path='/api/admin/pending'; desc='Admin pending users' },
    @{ method='POST'; path='/api/admin/approve/1'; desc='Admin approve (id=1)' },

    @{ method='POST'; path='/api/trips'; desc='Create trip (needs body/auth)'; body=@{ tripRequest=@{ purpose='smoke'; destination='Nowhere'; startDate=(Get-Date).ToString('yyyy-MM-dd'); endDate=(Get-Date).AddDays(1).ToString('yyyy-MM-dd') }; legs=@(); visas=@() } },
    @{ method='POST'; path='/api/trips/1/submit'; desc='Submit trip id=1' },
    @{ method='POST'; path='/api/trips/1/approve'; desc='Approve trip id=1' },
    @{ method='POST'; path='/api/trips/1/reject'; desc='Reject trip id=1' },
    @{ method='POST'; path='/api/trips/1/complete'; desc='Complete trip id=1' },
    @{ method='POST'; path='/api/trips/1/cancel'; desc='Cancel trip id=1' },
    @{ method='GET';  path='/api/trips/my-trips'; desc='My trips' },
    @{ method='GET';  path='/api/trips/pending-approvals'; desc='Pending approvals' },
    @{ method='GET';  path='/api/trips/1'; desc='Get trip id=1' },
    @{ method='GET';  path='/api/trips/1/legs'; desc='Get trip legs' },
    @{ method='GET';  path='/api/trips/1/visas'; desc='Get trip visas' },
    @{ method='POST'; path='/api/trips/1/legs/1/book'; desc='Book leg' },
    @{ method='POST'; path='/api/trips/1/visas/1'; desc='Update visa' },

    @{ method='POST'; path='/api/advances?tripRequestId=1'; desc='Create advance' ; body=@{ amount=100; currency='USD'; reason='smoke' } },
    @{ method='POST'; path='/api/advances/1/approve'; desc='Approve advance' },
    @{ method='POST'; path='/api/advances/1/disburse'; desc='Disburse advance' },
    @{ method='POST'; path='/api/advances/1/settle'; desc='Settle advance'; body=@{ amount=100 } },
    @{ method='POST'; path='/api/advances/1/forfeit'; desc='Forfeit advance' },
    @{ method='GET';  path='/api/advances/my-advances'; desc='My advances' },
    @{ method='GET';  path='/api/advances/1'; desc='Get advance' },

    @{ method='POST'; path='/api/expenses?tripRequestId=1'; desc='Create expense claim'; body=@{ total=10 } },
    @{ method='POST'; path='/api/expenses/1/lines'; desc='Add expense line'; body=@{ amount=10; category='MEAL' } },
    @{ method='POST'; path='/api/expenses/1/submit'; desc='Submit expense claim' },
    @{ method='POST'; path='/api/expenses/1/approve'; desc='Approve expense claim' },
    @{ method='POST'; path='/api/expenses/1/reject'; desc='Reject expense claim' },
    @{ method='POST'; path='/api/expenses/1/reimburse'; desc='Reimburse claim' ; body=@{ amount=10 } },
    @{ method='GET';  path='/api/expenses/my-claims'; desc='My claims' },
    @{ method='GET';  path='/api/expenses/1'; desc='Get claim' },
    @{ method='GET';  path='/api/expenses/1/lines'; desc='Get claim lines' },

    @{ method='POST'; path='/api/reports?title=smoke&reportType=summary'; desc='Generate report' },
    @{ method='GET';  path='/api/reports'; desc='List reports' },
    @{ method='GET';  path='/api/reports/type/summary'; desc='Reports by type' },

    @{ method='GET';  path='/api/notifications'; desc='Get notifications' },
    @{ method='GET';  path='/api/notifications/unread'; desc='Get unread notifications' },
    @{ method='POST'; path='/api/notifications/1/read'; desc='Mark notification read' }
)

function Send-Request($t) {
    $uri = $base + $t.path
    try {
        if ($t.method -eq 'GET') {
            $resp = Invoke-WebRequest -Uri $uri -Method GET -UseBasicParsing -TimeoutSec 15 -ErrorAction Stop
            return @{ status=$resp.StatusCode; body=$resp.Content }
        } elseif ($t.method -eq 'POST') {
            if ($t.body) {
                $json = $t.body | ConvertTo-Json -Depth 5
                $resp = Invoke-WebRequest -Uri $uri -Method POST -Body $json -ContentType 'application/json' -UseBasicParsing -TimeoutSec 15 -ErrorAction Stop
            } else {
                # Send empty POST
                $resp = Invoke-WebRequest -Uri $uri -Method POST -UseBasicParsing -TimeoutSec 15 -ErrorAction Stop
            }
            return @{ status=$resp.StatusCode; body=$resp.Content }
        } else {
            return @{ status='METHOD-NOT-SUPPORTED' }
        }
    } catch {
        # Try to extract HTTP status code from exception
        $ex = $_.Exception
        $status = 'ERROR'
        try {
            if ($ex.Response -and $ex.Response.StatusCode) { $status = $ex.Response.StatusCode.value__ }
        } catch {}
        return @{ status=$status; error=$_.Exception.Message }
    }
}

Write-Host "Starting smoke tests against $base ..."

foreach ($t in $tests) {
    Write-Host "Testing $($t.method) $($t.path) - $($t.desc)"
    $r = Send-Request $t
    $results += @{ method=$t.method; path=$t.path; desc=$t.desc; status=$r.status; error=($r.error -as [string]); body=($r.body -as [string]) }
}

# Write results to file
$results | ConvertTo-Json -Depth 5 | Out-File -FilePath .\tools\smoke_test_results.json -Encoding utf8

Write-Host "Smoke tests complete. Results written to .\tools\smoke_test_results.json"
Write-Host "Summary:"
$results | Group-Object -Property status | ForEach-Object { Write-Host "Status $($_.Name): $($_.Count)" }

# Also print a quick table
$results | Format-Table method, path, status -AutoSize
