async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const data = await res.json();

        if (res.ok) {
            currentUser = data;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            showNotification('Login successful!', 'success');
            setTimeout(() => {
                window.location.href = data.role === 'ADMIN' ? 'admin.html' : 'student.html';
            }, 1000);
        } else {
            showNotification(data.error || 'Login failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error. Is Spring Boot running?', 'danger');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('regName').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const studentId = document.getElementById('regStudentId').value;

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, studentId })
        });
        const data = await res.json();

        if (res.ok) {
            showNotification('Registration successful! Please login.', 'success');
            document.getElementById('registerForm').reset();
            document.getElementById('loginEmail').value = email;
        } else {
            showNotification(data.error || 'Registration failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error. Is Spring Boot running?', 'danger');
    }
}
