const API_BASE = 'http://localhost:8080/api';
let currentUser = null;

// Initialize user session
function initSession() {
    const saved = localStorage.getItem('currentUser');
    if (saved) {
        currentUser = JSON.parse(saved);
        return currentUser;
    }
    return null;
}

// Redirect if not logged in
function checkAuth(role) {
    const user = initSession();
    if (!user) {
        window.location.href = 'index.html';
        return;
    }
    if (role && user.role !== role) {
        window.location.href = user.role === 'ADMIN' ? 'admin.html' : 'student.html';
        return;
    }

    const userInfo = document.getElementById('userInfo');
    if (userInfo) {
        userInfo.textContent = `Hello, ${user.name}`;
    }
}

function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = 'index.html';
}

function showNotification(message, type = 'success') {
    const notif = document.getElementById('notification');
    if (!notif) return;
    
    // type can be success, danger (error), info
    const cssClass = type === 'success' ? 'notification-success' : 'notification-error';
    notif.className = `notification-bar ${cssClass}`;
    notif.innerHTML = `<i class="bi ${type === 'success' ? 'bi-check-circle' : 'bi-exclamation-triangle'} me-2"></i> ${message}`;
    notif.style.display = 'block';
    
    setTimeout(() => {
        notif.style.display = 'none';
    }, 4000);
}
