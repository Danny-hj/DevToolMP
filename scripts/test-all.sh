#!/bin/bash

echo "Running all tests..."

echo "=========================================="
echo "Backend Tests"
echo "=========================================="
cd backend
./mvnw test
BACKEND_RESULT=$?

echo ""
echo "=========================================="
echo "Frontend Tests"
echo "=========================================="
cd ../frontend
npm run test:unit
FRONTEND_RESULT=$?

echo ""
echo "=========================================="
echo "Test Results"
echo "=========================================="
echo "Backend: $([ $BACKEND_RESULT -eq 0 ] && echo 'PASSED' || echo 'FAILED')"
echo "Frontend: $([ $FRONTEND_RESULT -eq 0 ] && echo 'PASSED' || echo 'FAILED')"
echo "=========================================="

if [ $BACKEND_RESULT -eq 0 ] && [ $FRONTEND_RESULT -eq 0 ]; then
    echo "All tests passed!"
    exit 0
else
    echo "Some tests failed!"
    exit 1
fi
