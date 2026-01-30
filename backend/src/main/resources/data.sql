INSERT INTO categories (name, description, icon, sort_order) VALUES
('开发工具', '各类开发工具和 IDE 插件', 'code', 1),
('构建工具', '构建、打包和部署工具', 'hammer', 2),
('测试工具', '测试框架和测试工具', 'bug', 3),
('文档工具', '文档生成和文档工具', 'document', 4),
('其他', '其他工具', 'apps', 5);

INSERT INTO users (username, email, password, bio, github_url) VALUES
('testuser1', 'test1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '开发者', 'https://github.com/testuser1'),
('testuser2', 'test2@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '工程师', 'https://github.com/testuser2');

INSERT INTO tools (name, description, category_id, github_owner, github_repo, version, stars, forks, open_issues, watchers, view_count, favorite_count, install_count, status) VALUES
('React DevTools', 'React 开发者工具浏览器扩展', 1, 'facebook', 'react', '18.2.0', 180000, 35000, 500, 12000, 5000, 800, 2000, 'active'),
('Webpack', '现代 JavaScript 应用程序的模块打包器', 2, 'webpack', 'webpack', '5.89.0', 63000, 8500, 200, 3500, 3000, 600, 1500, 'active'),
('Jest', 'JavaScript 测试框架', 3, 'facebook', 'jest', '29.7.0', 42000, 5500, 300, 2800, 2500, 500, 1200, 'active'),
('Vite', '下一代前端构建工具', 2, 'vitejs', 'vite', '5.0.0', 60000, 4000, 150, 2000, 4000, 700, 1800, 'active'),
('Vue DevTools', 'Vue.js 开发者工具', 1, 'vuejs', 'devtools', '6.5.0', 25000, 3000, 100, 800, 2000, 400, 1000, 'active');

INSERT INTO tool_tags (tool_id, tag_name) VALUES
(1, 'react'),
(1, 'browser-extension'),
(1, 'devtools'),
(2, 'webpack'),
(2, 'bundler'),
(2, 'build-tools'),
(3, 'jest'),
(3, 'testing'),
(3, 'unit-test'),
(4, 'vite'),
(4, 'build-tool'),
(4, 'esbuild'),
(5, 'vue'),
(5, 'devtools'),
(5, 'browser-extension');

INSERT INTO ratings (tool_id, user_id, score, comment) VALUES
(1, 1, 5, '非常好用的开发工具，提高了开发效率！'),
(1, 2, 4, '功能强大，但有时候会有卡顿'),
(2, 1, 4, '配置有点复杂，但是功能很全面'),
(3, 2, 5, '测试框架的标杆，简单易用'),
(4, 1, 5, '速度超快，开发体验非常好！');

INSERT INTO comment_replies (rating_id, user_id, reply_to_user_id, content) VALUES
(1, 2, 1, '完全同意，React DevTools 是必备工具！');
(3, 2, 1, '推荐查看官方文档，有很多配置示例');
