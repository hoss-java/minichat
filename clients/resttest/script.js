// Initialize theme on page load
function initializeTheme() {
  const savedTheme = localStorage.getItem('theme');
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  
  const theme = savedTheme || (prefersDark ? 'dark' : 'light');
  setTheme(theme);
}

// Set theme and update DOM
function setTheme(theme) {
  if (theme === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark');
    updateToggleIcon('☀️');
  } else {
    document.documentElement.removeAttribute('data-theme');
    updateToggleIcon('🌙');
  }
  localStorage.setItem('theme', theme);
}

// Update toggle button icon
function updateToggleIcon(icon) {
  const toggleIcon = document.querySelector('.toggle-icon');
  if (toggleIcon) {
    toggleIcon.textContent = icon;
  }
}

// Get current theme
function getCurrentTheme() {
  return document.documentElement.getAttribute('data-theme') || 'light';
}

// Toggle theme
function toggleTheme() {
  const currentTheme = getCurrentTheme();
  const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
  setTheme(newTheme);
}

// Token Management
function loadTokenFromStorage() {
  const token = localStorage.getItem('apiToken');
  if (token) {
    document.getElementById('token').value = token;
  }
}

function saveToken() {
  const token = document.getElementById('token').value.trim();
  if (token) {
    localStorage.setItem('apiToken', token);
    showNotification('Token saved', 'success');
  } else {
    showNotification('Token is empty', 'error');
  }
}

function clearToken() {
  document.getElementById('token').value = '';
  localStorage.removeItem('apiToken');
  showNotification('Token cleared', 'success');
}

function toggleTokenVisibility() {
  const tokenInput = document.getElementById('token');
  const isPassword = tokenInput.type === 'password';
  tokenInput.type = isPassword ? 'text' : 'password';
  
  const btn = document.getElementById('toggle-token-visibility');
  btn.textContent = isPassword ? '🙈' : '👁️';
}

// Custom Headers Management
function addHeader() {
  const headersList = document.getElementById('headers-list');
  
  if (headersList.querySelector('.empty-message')) {
    headersList.innerHTML = '';
  }

  const headerItem = document.createElement('div');
  headerItem.className = 'header-item';
  headerItem.innerHTML = `
    <input type="text" class="header-key" placeholder="Header name" value="">
    <input type="text" class="header-value" placeholder="Header value" value="">
    <button class="icon-btn delete-header">✕</button>
  `;

  const deleteBtn = headerItem.querySelector('.delete-header');
  deleteBtn.addEventListener('click', () => {
    headerItem.remove();
    checkHeadersEmpty();
  });

  headersList.appendChild(headerItem);
}

function checkHeadersEmpty() {
  const headersList = document.getElementById('headers-list');
  const headers = headersList.querySelectorAll('.header-item');
  
  if (headers.length === 0) {
    headersList.innerHTML = '<div class="empty-message">No custom headers added</div>';
  }
}

function getCustomHeaders() {
  const customHeaders = {};
  document.querySelectorAll('.header-item').forEach(item => {
    const key = item.querySelector('.header-key').value.trim();
    const value = item.querySelector('.header-value').value.trim();
    if (key && value) {
      customHeaders[key] = value;
    }
  });
  return customHeaders;
}

function displayRequestHeaders(headers) {
  const headersList = document.getElementById('request-headers');
  const headerStr = Object.entries(headers)
    .map(([key, value]) => `${key}: ${value}`)
    .join('\n');
  
  headersList.textContent = headerStr || 'No headers';
}

function displayResponseHeaders(response) {
  const responseHeadersDiv = document.getElementById('response-headers');
  const headers = {};
  
  response.headers.forEach((value, key) => {
    headers[key] = value;
  });
  
  const headerStr = Object.entries(headers)
    .map(([key, value]) => `${key}: ${value}`)
    .join('\n');
  
  responseHeadersDiv.textContent = headerStr || 'No headers';
}

// Tab Management
function switchTab(tabName) {
  // Hide all tabs
  document.querySelectorAll('.tab-content').forEach(tab => {
    tab.classList.remove('active');
  });
  
  // Remove active class from all buttons
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.remove('active');
  });
  
  // Show selected tab
  const selectedTab = document.getElementById(tabName);
  if (selectedTab) {
    selectedTab.classList.add('active');
  }
  
  // Add active class to clicked button
  if (event && event.target) {
    event.target.classList.add('active');
  }
}

// Make API request
async function makeRequest() {
  const method = document.getElementById('method').value;
  const endpoint = document.getElementById('endpoint').value;
  const body = document.getElementById('body').value;
  const token = document.getElementById('token').value;
  const responseDiv = document.getElementById('response');
  const responseBody = document.getElementById('responseBody');

  // Reset response classes
  responseDiv.classList.remove('show', 'success', 'error');

  // Validate endpoint
  if (!endpoint.trim()) {
    responseBody.textContent = 'Error: Endpoint is required';
    responseDiv.classList.add('show', 'error');
    return;
  }

  const startTime = performance.now();
  const timestamp = new Date().toLocaleString();

  try {
    const options = {
      method: method,
      headers: {
        'Content-Type': 'application/json',
      },
    };

    // Add Authorization header if token exists
    if (token.trim()) {
      options.headers['Authorization'] = `Bearer ${token.trim()}`;
    }

    // Add custom headers
    const customHeaders = getCustomHeaders();
    Object.assign(options.headers, customHeaders);

    // Add body for non-GET requests
    if (method !== 'GET' && body.trim()) {
      try {
        JSON.parse(body);
        options.body = body;
      } catch (e) {
        responseBody.textContent = 'Error: Invalid JSON in request body';
        responseDiv.classList.add('show', 'error');
        return;
      }
    }

    // Display request details
    displayRequestDetails(method, endpoint, options.headers, body, timestamp);

    const response = await fetch(`proxy.php?path=${encodeURIComponent(endpoint)}`, options);
    const data = await response.text();
    const endTime = performance.now();
    const responseTime = (endTime - startTime).toFixed(2);

    // Try to pretty-print JSON
    let displayData = data;
    try {
      displayData = JSON.stringify(JSON.parse(data), null, 2);
    } catch (e) {
      // Not JSON, display as is
    }

    responseBody.textContent = displayData;
    responseDiv.classList.add('show', response.ok ? 'success' : 'error');

    // Display response headers
    displayResponseHeaders(response);

    // Update status badge
    updateResponseStatus(response.status, responseTime);

    // Add to history
    addToHistory(method, endpoint, response.status);

  } catch (error) {
    responseBody.textContent = `Error: ${error.message}`;
    responseDiv.classList.add('show', 'error');
    updateResponseStatus('ERROR', '—');
  }
}

// Display request details
function displayRequestDetails(method, endpoint, headers, body, timestamp) {
  document.getElementById('request-timestamp').textContent = timestamp;
  document.getElementById('request-url').textContent = `${method} /api${endpoint}`;
  
  displayRequestHeaders(headers);
  
  if (!body.trim()) {
    document.getElementById('body').value = '';
  }
}

// Update response status display
function updateResponseStatus(status, time) {
  const statusBadge = document.getElementById('response-status');
  const timeDisplay = document.getElementById('response-time');

  statusBadge.textContent = status;
  statusBadge.className = 'status-badge';
  
  if (status === 'ERROR') {
    statusBadge.classList.add('error');
  } else if (status >= 400) {
    statusBadge.classList.add('error');
  } else if (status >= 300) {
    statusBadge.classList.add('warning');
  } else {
    statusBadge.classList.add('success');
  }

  timeDisplay.textContent = `${time}ms`;
}

// Request History
function addToHistory(method, endpoint, status) {
  let history = JSON.parse(localStorage.getItem('requestHistory') || '[]');
  
  history.unshift({
    method,
    endpoint,
    status,
    timestamp: new Date().toLocaleTimeString()
  });

  // Keep only last 10 requests
  history = history.slice(0, 10);
  localStorage.setItem('requestHistory', JSON.stringify(history));
  
  renderHistory();
}

function renderHistory() {
  const historyList = document.getElementById('history-list');
  const history = JSON.parse(localStorage.getItem('requestHistory') || '[]');

  historyList.innerHTML = '';

  if (history.length === 0) {
    historyList.innerHTML = '<li class="empty-history">No requests yet</li>';
    return;
  }

  history.forEach((item) => {
    const li = document.createElement('li');
    li.className = `history-item status-${item.status < 400 ? 'success' : 'error'}`;
    li.innerHTML = `
      <span class="history-method">${item.method}</span>
      <span class="history-endpoint">${item.endpoint}</span>
      <span class="history-status">${item.status}</span>
      <span class="history-time">${item.timestamp}</span>
    `;
    li.addEventListener('click', () => {
      document.getElementById('method').value = item.method;
      document.getElementById('endpoint').value = item.endpoint;
    });
    historyList.appendChild(li);
  });
}

function clearHistory() {
  localStorage.removeItem('requestHistory');
  renderHistory();
  showNotification('History cleared', 'success');
}

// Copy response to clipboard
function copyResponse() {
  const responseBody = document.getElementById('responseBody').textContent;
  navigator.clipboard.writeText(responseBody).then(() => {
    showNotification('Response copied to clipboard', 'success');
  }).catch(() => {
    showNotification('Failed to copy', 'error');
  });
}

// Notification display
function showNotification(message, type) {
  const notification = document.createElement('div');
  notification.className = `notification ${type}`;
  notification.textContent = message;
  
  document.body.appendChild(notification);
  
  setTimeout(() => {
    notification.remove();
  }, 3000);
}

// Preset buttons
function loadPreset(endpoint, method) {
  document.getElementById('endpoint').value = endpoint;
  document.getElementById('method').value = method;
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
  // Initialize theme
  initializeTheme();

  // Theme toggle
  const themeToggle = document.getElementById('theme-toggle');
  if (themeToggle) {
    themeToggle.addEventListener('click', toggleTheme);
  }

  // Token management
  loadTokenFromStorage();
  document.getElementById('toggle-token-visibility').addEventListener('click', toggleTokenVisibility);
  document.getElementById('save-token').addEventListener('click', saveToken);
  document.getElementById('clear-token').addEventListener('click', clearToken);

  // Custom headers
  document.getElementById('add-header-btn').addEventListener('click', addHeader);

  // Tab switching
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const tabName = e.target.dataset.tab;
      switchTab(tabName);
    });
  });

  // Send request
  const sendBtn = document.getElementById('send-btn');
  if (sendBtn) {
    sendBtn.addEventListener('click', makeRequest);
  }

  // Copy response
  document.getElementById('copy-response').addEventListener('click', copyResponse);

  // Clear history
  document.getElementById('clear-history').addEventListener('click', clearHistory);

  // Preset buttons
  document.querySelectorAll('.preset-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const endpoint = e.target.dataset.endpoint;
      const method = e.target.dataset.method;
      loadPreset(endpoint, method);
    });
  });

  // Render initial history
  renderHistory();

  // Allow Enter key to submit with Ctrl+Enter
  const bodyTextarea = document.getElementById('body');
  if (bodyTextarea) {
    bodyTextarea.addEventListener('keydown', (e) => {
      if (e.ctrlKey && e.key === 'Enter') {
        makeRequest();
      }
    });
  }
});

// Listen for system theme changes
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
  if (!localStorage.getItem('theme')) {
    setTheme(e.matches ? 'dark' : 'light');
  }
});
