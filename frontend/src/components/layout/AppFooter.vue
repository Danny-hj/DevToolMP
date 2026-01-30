<template>
  <footer class="app-footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-section">
          <h3>DevToolMP</h3>
          <p>工具市场平台</p>
        </div>
        <div class="footer-section">
          <h4>链接</h4>
          <ul>
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/tools">工具列表</router-link></li>
          </ul>
        </div>
        <div class="footer-section">
          <h4>关于</h4>
          <p>© 2024 DevToolMP</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.app-footer {
  background-color: #1a1a1a;
  border-top: 1px solid #333;
  padding: 40px 0;
  margin-top: 60px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
}

.footer-section h3 {
  font-size: 18px;
  margin: 0 0 10px;
  color: #409eff;
}

.footer-section h4 {
  font-size: 14px;
  margin: 0 0 10px;
  color: #e0e0e0;
}

.footer-section p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section li {
  margin-bottom: 8px;
}

.footer-section a {
  color: #909399;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: #409eff;
}
</style>
