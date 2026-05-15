let allCourses = [];
let allEnrollments = [];
let enrollModal, slipModal, materialsModal;

document.addEventListener('DOMContentLoaded', () => {
    checkAuth('STUDENT');

    // Initialize Modals
    enrollModal = new bootstrap.Modal(document.getElementById('enrollModal'));
    slipModal = new bootstrap.Modal(document.getElementById('slipModal'));
    materialsModal = new bootstrap.Modal(document.getElementById('materialsModal'));

    loadStudentDashboard();
});

async function loadStudentDashboard() {
    const welcomeName = document.getElementById('studentWelcomeName');
    if (welcomeName) welcomeName.textContent = currentUser.name;
    await Promise.all([
        loadCourses(),
        loadMyEnrollments(),
        loadProfile()
    ]);
    updateStudentStats();
}

async function loadCourses() {
    try {
        const res = await fetch(`${API_BASE}/student/courses`);
        allCourses = await res.json();
        renderCourses(allCourses);
    } catch (err) {
        console.error('Error loading courses:', err);
    }
}

function renderCourses(courses) {
    const container = document.getElementById('coursesList');
    if (courses.length === 0) {
        container.innerHTML = '<div class="col-12 text-center text-secondary py-5">No available courses found.</div>';
        return;
    }
    container.innerHTML = courses.map(course => `
        <div class="col-md-6 col-lg-4">
            <div class="glass-card h-100 d-flex flex-column">
                <div class="mb-3">
                    <h5 class="fw-bold text-white mb-2">${course.title}</h5>
                    <p class="text-secondary small mb-3">${course.description || 'No detailed description provided.'}</p>
                </div>
                <div class="mt-auto">
                    <div class="d-flex justify-content-between mb-3 small text-secondary">
                        <span><i class="bi bi-calendar3 me-2 text-cyan"></i>${course.schedule || 'TBA'}</span>
                        <span><i class="bi bi-cpu me-2 text-pink"></i>Cap: ${course.capacity}</span>
                    </div>
                    <button class="btn-primary-neon w-100" onclick="openEnrollModal(${course.id}, '${course.title.replace(/'/g, "\\'")}')">
                        <i class="bi bi-box-arrow-in-right"></i>
                        <span>Enroll</span>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

function searchCourses() {
    const keyword = document.getElementById('courseSearchInput').value;
    fetch(`${API_BASE}/student/courses?search=${encodeURIComponent(keyword)}`)
        .then(r => r.json())
        .then(data => renderCourses(data))
        .catch(err => showNotification('Error searching courses', 'danger'));
}

function openEnrollModal(courseId, title) {
    document.getElementById('enrollForm').reset();
    document.getElementById('enrollCourseId').value = courseId;
    document.getElementById('enrollCourseTitle').value = title;
    enrollModal.show();
}

async function submitEnrollment(e) {
    e.preventDefault();
    const courseId = document.getElementById('enrollCourseId').value;
    const fileInput = document.getElementById('enrollPaymentSlip');

    const formData = new FormData();
    formData.append('studentId', currentUser.id);
    formData.append('courseId', courseId);
    formData.append('paymentSlip', fileInput.files[0]);

    try {
        const res = await fetch(`${API_BASE}/student/enroll`, {
            method: 'POST',
            body: formData
        });
        const data = await res.json();

        if (res.ok) {
            showNotification('Enrollment submitted! Waiting for admin approval.', 'success');
            enrollModal.hide();
            loadMyEnrollments();
        } else {
            showNotification(data.error || 'Enrollment failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function loadMyEnrollments() {
    try {
        const res = await fetch(`${API_BASE}/student/enrollments/${currentUser.id}`);
        allEnrollments = await res.json();

        // Split enrollments into Pending and Finalized
        const pending = allEnrollments.filter(e => e.status === 'PENDING');
        const finalized = allEnrollments.filter(e => e.status !== 'PENDING');

        renderEnrollments(finalized);
        renderPendingEnrollments(pending);
    } catch (err) {
        console.error('Error loading enrollments:', err);
    }
}

function renderEnrollments(enrollments) {
    const tbody = document.getElementById('enrollmentsTableBody');
    if (enrollments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-secondary py-5">No approved courses found.</td></tr>';
        return;
    }
    tbody.innerHTML = enrollments.map(e => {
        const statusBadge = e.status === 'APPROVED' ? 'badge-approved' :
            e.status === 'REJECTED' ? 'badge-rejected' : 'badge-pending';

        const slipPath = e.paymentSlip?.filePath || e.paymentSlipPath || '';
        return `
            <tr>
                <td><div class="fw-bold text-white">${e.courseName || `Course #${e.courseId}`}</div></td>
                <td><span class="status-badge ${statusBadge}">${e.status}</span></td>
                <td>
                    ${slipPath ? `
                    <button class="btn btn-sm btn-outline-neon" onclick="viewSlip('${slipPath.replace(/\\/g, '\\\\')}')">
                        <i class="bi bi-file-earmark-medical me-1"></i>View Slip
                    </button>` : '<span class="text-secondary">-</span>'}
                </td>
                <td>
                    ${e.status === 'APPROVED' ? `
                        <button class="btn btn-sm btn-outline-neon text-cyan border-cyan" onclick="viewStudentMaterials(${e.courseId})" style="--accent-cyan: rgba(0, 242, 255, 0.4);">
                            <i class="bi bi-folder2-open me-1"></i>View Materials
                        </button>
                    ` : '<i class="bi bi-lock-fill text-secondary"></i>'}
                </td>
                <td><i class="bi bi-clock-history me-2 text-secondary"></i>${e.enrolledAt ? new Date(e.enrolledAt).toLocaleDateString() : 'N/A'}</td>
                <td>
                    ${e.status !== 'DROPPED' ? `
                        <button class="action-icon-btn delete" onclick="dropEnrollment(${e.id})" title="Drop Course">
                            <i class="bi bi-x-circle"></i>
                        </button>
                    ` : '<span class="text-secondary small">Inactive</span>'}
                </td>
            </tr>
        `;
    }).join('');
}

function renderPendingEnrollments(enrollments) {
    const tbody = document.getElementById('pendingTableBody');
    if (enrollments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-secondary py-5">No pending requests found.</td></tr>';
        return;
    }
    tbody.innerHTML = enrollments.map(e => {
        const slipPath = e.paymentSlip?.filePath || e.paymentSlipPath || '';
        return `
            <tr>
                <td><div class="fw-bold text-white">${e.courseName || `Course #${e.courseId}`}</div></td>
                <td><span class="status-badge badge-pending">PENDING</span></td>
                <td>
                    ${slipPath ? `
                    <button class="btn btn-sm btn-outline-neon" onclick="viewSlip('${slipPath.replace(/\\/g, '\\\\')}')">
                        <i class="bi bi-file-earmark-medical me-1"></i>View Slip
                    </button>` : '<span class="text-secondary">No document</span>'}
                </td>
                <td><i class="bi bi-clock-history me-2 text-secondary"></i>${e.enrolledAt ? new Date(e.enrolledAt).toLocaleDateString() : 'N/A'}</td>
                <td>
                    <button class="action-icon-btn delete" onclick="dropEnrollment(${e.id})" title="Cancel Request">
                        <i class="bi bi-trash3"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function viewSlip(path) {
    const img = document.getElementById('slipImage');
    const pdf = document.getElementById('slipPdf');
    if (path.toLowerCase().endsWith('.pdf')) {
        img.style.display = 'none';
        pdf.style.display = 'block';
        pdf.src = path;
    } else {
        pdf.style.display = 'none';
        img.style.display = 'block';
        img.src = path;
    }
    slipModal.show();
}

async function viewStudentMaterials(courseId) {
    const container = document.getElementById('materialsListContainer');
    document.getElementById('materialsModalTitle').textContent = 'Course Materials';
    container.innerHTML = '<p class="text-center text-muted">Loading...</p>';
    materialsModal.show();

    try {
        const res = await fetch(`${API_BASE}/student/courses/${courseId}/materials?studentId=${currentUser.id}`);
        const data = await res.json();
        if (res.ok) {
            if (data.length === 0) {
                container.innerHTML = '<div class="alert alert-info">No materials available for this course yet.</div>';
                return;
            }
            container.innerHTML = `
                <div class="list-group list-group-flush" style="background: transparent;">
                    ${data.map(m => `
                        <a href="${m.filePath}" target="_blank" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center bg-transparent border-color py-3" style="color: var(--text-primary); border-bottom: 1px solid var(--border-color);">
                            <span><i class="bi bi-file-earmark-text me-3 text-cyan"></i>${m.title}</span>
                            <i class="bi bi-download text-secondary"></i>
                        </a>
                    `).join('')}
                </div>
            `;
        } else {
            container.innerHTML = `<div class="alert alert-danger">${data.error || 'Access denied'}</div>`;
        }
    } catch (err) {
        container.innerHTML = '<div class="alert alert-danger">Error loading materials</div>';
    }
}

async function dropEnrollment(enrollmentId) {
    if (!confirm('Are you sure you want to drop this course?')) return;
    try {
        const res = await fetch(`${API_BASE}/student/drop/${enrollmentId}?studentId=${currentUser.id}`, {
            method: 'POST'
        });
        const data = await res.json();
        if (res.ok) {
            showNotification('Course dropped successfully', 'success');
            loadMyEnrollments();
        } else {
            showNotification(data.error || 'Failed to drop course', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function loadProfile() {
    try {
        const res = await fetch(`${API_BASE}/student/profile/${currentUser.id}`);
        const student = await res.json();
        document.getElementById('profileName').value = student.name || '';
        document.getElementById('profileEmail').value = student.email || '';
        document.getElementById('profileStudentId').value = student.studentId || '';
    } catch (err) {
        console.error('Error loading profile:', err);
    }
}

async function updateProfile(e) {
    e.preventDefault();
    const name = document.getElementById('profileName').value;
    const email = document.getElementById('profileEmail').value;

    try {
        const res = await fetch(`${API_BASE}/student/profile/${currentUser.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email })
        });
        if (res.ok) {
            showNotification('Profile updated successfully', 'success');
            currentUser.name = name;
            currentUser.email = email;
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
        } else {
            const data = await res.json();
            showNotification(data.error || 'Update failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function deleteAccount() {
    if (!confirm('WARNING: This will permanently delete your account and all enrollments. Continue?')) return;
    try {
        const res = await fetch(`${API_BASE}/student/profile/${currentUser.id}`, {
            method: 'DELETE'
        });
        if (res.ok) {
            showNotification('Account deleted. Goodbye!', 'info');
            logout();
        } else {
            const data = await res.json();
            showNotification(data.error || 'Delete failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function changePassword(e) {
    e.preventDefault();
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;

    try {
        const res = await fetch(`${API_BASE}/student/profile/${currentUser.id}/password`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ currentPassword, newPassword })
        });
        if (res.ok) {
            showNotification('Password updated successfully', 'success');
            document.getElementById('passwordForm').reset();
        } else {
            const data = await res.json();
            showNotification(data.error || 'Password update failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

function updateStudentStats() {
    const pendingCount = allEnrollments.filter(e => e.status === 'PENDING').length;
    const activeCount = allEnrollments.filter(e => e.status === 'APPROVED').length;

    document.getElementById('statTotalCourses').textContent = allCourses.length;
    document.getElementById('statMyEnrollments').textContent = activeCount;
    document.getElementById('statPending').textContent = pendingCount;
}
