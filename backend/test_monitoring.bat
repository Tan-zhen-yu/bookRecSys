@echo off
echo 测试监控数据生成...

echo 1. 访问测试接口产生流量
for /l %%i in (1,1,10) do (
    echo 请求 %%i
    curl -s http://localhost:8080/test/unstable >nul 2>&1
    timeout /t 1 >nul
)

echo.
echo 2. 检查监控数据
curl -s http://localhost:8080/monitoring/traffic

echo.
echo 3. 检查熔断器状态
curl -s http://localhost:8080/monitoring/circuit-breakers

pause
