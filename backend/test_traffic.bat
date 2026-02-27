@echo off
echo 开始产生测试流量...

for /l %%i in (1,1,50) do (
    echo 发送第 %%i 次请求...
    curl -s http://localhost:8080/monitoring/traffic >nul
    timeout /t 1 >nul
)

echo 流量测试完成！
pause
