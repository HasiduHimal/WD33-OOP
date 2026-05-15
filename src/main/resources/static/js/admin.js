let editCourseModal, courseStudentsModal, slipModal;

document.addEventListener('DOMContentLoaded', () => {
    checkAuth('ADMIN');

    // Initialize Modals
    editCourseModal = new bootstrap.Modal(document.getElementById('editCourseModal'));
    courseStudentsModal = new bootstrap.Modal(document.getElementById('courseStudentsModal'));
    slipModal = new bootstrap.Modal(document.getElementById('slipModal'));

    loadAdminDashboard();
});

async function loadAdminDashboard() {
    const welcomeName = document.getElementById('adminWelcomeName');
    if (welcomeName) welcomeName.textContent = currentUser.name;
    await Promise.all([
        loadAdminCourses(),
        loadAdminStudents(),
        loadPendingEnrollments()
    ]);
    updateAdminStats();
}

async function loadAdminCourses() {
    try {
        const res = await fetch(`${API_BASE}/admin/courses`);
        const courses = await res.json();
        renderAdminCourses(courses);
        updateMaterialCourseSelect(courses);
    } catch (err) {
        console.error('Error loading admin courses:', err);
    }
}

function renderAdminCourses(courses) {
    const tbody = document.getElementById('adminCoursesTableBody');
    if (courses.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-secondary py-5">No active courses found.</td></tr>';
        return;
    }
    tbody.innerHTML = courses.map(c => `
        <tr>
            <td>
                <div class="fw-bold text-white">${c.title}</div>
                <div class="small text-secondary">${(c.description || '').substring(0, 60)}${(c.description || '').length > 60 ? '...' : ''}</div>
            </td>
            <td><i class="bi bi-calendar3 me-2 text-secondary"></i>${c.schedule || 'N/A'}</td>
            <td><i class="bi bi-cpu me-2 text-secondary"></i>${c.capacity}</td>
            <td>
                <button class="btn btn-sm btn-outline-info border-0" onclick="viewCourseStudents(${c.id}, '${c.title.replace(/'/g, "\\'")}')">
                    <i class="bi bi-people-fill me-1"></i>${(c.students || []).length} Students
                </button>
            </td>
            <td>
                <div class="d-flex">
                    <button class="action-icon-btn edit" title="Edit Course" onclick="openEditCourseModal(${c.id}, '${c.title.replace(/'/g, "\\'")}', '${(c.description || '').replace(/'/g, "\\'")}', '${(c.schedule || '').replace(/'/g, "\\'")}', ${c.capacity})">
                        <i class="bi bi-pencil-square"></i>
                    </button>
                    <button class="action-icon-btn delete" title="Delete Course" onclick="deleteCourse(${c.id})">
                        <i class="bi bi-trash3"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function updateMaterialCourseSelect(courses) {
    const select = document.getElementById('materialCourseId');
    if (select) {
        select.innerHTML = '<option value="">Choose course...</option>' +
            courses.map(c => `<option value="${c.id}">${c.title}</option>`).join('');
    }
}

async function addCourse(e) {
    e.preventDefault();
    const payload = {
        title: document.getElementById('courseTitle').value,
        description: document.getElementById('courseDescription').value,
        schedule: document.getElementById('courseSchedule').value,
        capacity: parseInt(document.getElementById('courseCapacity').value),
        adminId: currentUser.id
    };

    try {
        const res = await fetch(`${API_BASE}/admin/courses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            showNotification('Course added successfully', 'success');
            document.getElementById('addCourseForm').reset();
            loadAdminCourses();
        } else {
            const data = await res.json();
            showNotification(data.error || 'Failed to add course', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

function openEditCourseModal(id, title, description, schedule, capacity) {
    document.getElementById('editCourseId').value = id;
    document.getElementById('editCourseTitle').value = title;
    document.getElementById('editCourseDescription').value = description;
    document.getElementById('editCourseSchedule').value = schedule;
    document.getElementById('editCourseCapacity').value = capacity;
    editCourseModal.show();
}

async function updateCourse(e) {
    e.preventDefault();
    const courseId = document.getElementById('editCourseId').value;
    const payload = {
        title: document.getElementById('editCourseTitle').value,
        description: document.getElementById('editCourseDescription').value,
        schedule: document.getElementById('editCourseSchedule').value,
        capacity: parseInt(document.getElementById('editCourseCapacity').value)
    };

    try {
        const res = await fetch(`${API_BASE}/admin/courses/${courseId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            showNotification('Course updated successfully', 'success');
            editCourseModal.hide();
            loadAdminCourses();
        } else {
            const data = await res.json();
            showNotification(data.error || 'Update failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function deleteCourse(courseId) {
    if (!confirm('Delete this course? All enrollments and materials will also be removed.')) return;
    try {
        const res = await fetch(`${API_BASE}/admin/courses/${courseId}`, { method: 'DELETE' });
        if (res.ok) {
            showNotification('Course deleted', 'success');
            loadAdminCourses();
        } else {
            const data = await res.json();
            showNotification(data.error || 'Delete failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function loadAdminStudents() {
    try {
        const res = await fetch(`${API_BASE}/admin/students`);
        const students = await res.json();
        renderAdminStudents(students);
        document.getElementById('adminStatStudents').textContent = students.length;
    } catch (err) {
        console.error('Error loading students:', err);
    }
}

function renderAdminStudents(students) {
    const tbody = document.getElementById('adminStudentsTableBody');
    if (students.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-secondary py-5">No registered students found.</td></tr>';
        return;
    }
    tbody.innerHTML = students.map(s => `
        <tr>
            <td><span class="badge-approved status-badge" style="font-family: monospace;">${s.studentId || 'GEN-X'}</span></td>
            <td><div class="fw-bold">${s.name}</div></td>
            <td><i class="bi bi-envelope-at me-2 text-secondary"></i>${s.email}</td>
            <td>
                <div class="d-flex align-items-center">
                    <div class="progress flex-grow-1 me-3" style="height: 6px; background: rgba(255,255,255,0.05);">
                        <div class="progress-bar" style="width: ${(s.courses || []).length * 20}%; background: var(--accent-cyan); box-shadow: var(--glow-cyan);"></div>
                    </div>
                    <span>${(s.courses || []).length} Courses</span>
                </div>
            </td>
        </tr>
    `).join('');
}

async function loadPendingEnrollments() {
    try {
        const res = await fetch(`${API_BASE}/admin/enrollments/pending`);
        const enrollments = await res.json();
        renderPendingEnrollments(enrollments);
        document.getElementById('adminStatPending').textContent = enrollments.length;
    } catch (err) {
        console.error('Error loading pending enrollments:', err);
    }
}

function renderPendingEnrollments(enrollments) {
    const tbody = document.getElementById('pendingEnrollmentsTableBody');
    if (enrollments.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-secondary py-5">All pending requests processed.</td></tr>';
        return;
    }
    tbody.innerHTML = enrollments.map(e => {
        const slipPath = e.paymentSlip?.filePath || e.paymentSlipPath || '';
        return `
            <tr>
                <td><span class="text-secondary" style="font-family: monospace;">#${e.id.toString().padStart(4, '0')}</span></td>
                <td>
                    <div class="fw-bold">${e.studentName || 'Unknown Student'}</div>
                    <div class="small text-secondary">${e.studentCode || `REF: ${e.studentId}`}</div>
                </td>
                <td><span class="text-cyan">${e.courseName || `Course #${e.courseId}`}</span></td>
                <td>
                    ${slipPath ? `
                    <button class="btn btn-sm btn-outline-info" onclick="viewSlip('${slipPath.replace(/\\/g, '\\\\')}')" style="border-radius: 8px;">
                        <i class="bi bi-file-earmark-medical me-1"></i>Verify Slip
                    </button>` : '<span class="text-secondary">No document</span>'}
                </td>
                <td><i class="bi bi-clock-history me-2 text-secondary"></i>${e.enrolledAt ? new Date(e.enrolledAt).toLocaleDateString() : 'N/A'}</td>
                <td>
                    <div class="d-flex gap-2">
                        <button class="btn btn-sm btn-success px-3" onclick="approveEnrollment(${e.id})" style="border-radius: 8px; background: rgba(0, 242, 255, 0.1); border-color: var(--accent-cyan); color: var(--accent-cyan);">
                            <i class="bi bi-shield-check me-1"></i>Approve
                        </button>
                        <button class="btn btn-sm btn-danger px-3" onclick="rejectEnrollment(${e.id})" style="border-radius: 8px; background: rgba(255, 77, 77, 0.1); border-color: #ff4d4d; color: #ff4d4d;">
                            <i class="bi bi-shield-x me-1"></i>Reject
                        </button>
                    </div>
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

async function approveEnrollment(enrollmentId) {
    try {
        const res = await fetch(`${API_BASE}/admin/enrollments/${enrollmentId}/approve`, { method: 'POST' });
        if (res.ok) {
            showNotification('Enrollment approved!', 'success');
            loadPendingEnrollments();
        } else {
            const data = await res.json();
            showNotification(data.error || 'Approval failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function rejectEnrollment(enrollmentId) {
    try {
        const res = await fetch(`${API_BASE}/admin/enrollments/${enrollmentId}/reject`, { method: 'POST' });
        if (res.ok) {
            showNotification('Enrollment rejected', 'info');
            loadPendingEnrollments();
        } else {
            const data = await res.json();
            showNotification(data.error || 'Rejection failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function uploadMaterial(e) {
    e.preventDefault();
    const courseId = document.getElementById('materialCourseId').value;
    const title = document.getElementById('materialTitle').value;
    const fileInput = document.getElementById('materialFile');

    const formData = new FormData();
    formData.append('courseId', courseId);
    formData.append('title', title);
    formData.append('file', fileInput.files[0]);

    try {
        const res = await fetch(`${API_BASE}/admin/materials`, {
            method: 'POST',
            body: formData
        });
        if (res.ok) {
            showNotification('Material uploaded successfully', 'success');
            document.getElementById('uploadMaterialForm').reset();
            loadAdminCourseMaterials(courseId); // Refresh list
        } else {
            const data = await res.json();
            showNotification(data.error || 'Upload failed', 'danger');
        }
    } catch (err) {
        showNotification('Server error', 'danger');
    }
}

async function loadAdminCourseMaterials(courseId) {
    const container = document.getElementById('adminMaterialsList');
    if (!courseId) {
        container.innerHTML = '<p class="text-muted">Select a course to view materials.</p>';
        return;
    }
    container.innerHTML = '<p class="text-center text-muted">Loading...</p>';

    try {
        const res = await fetch(`${API_BASE}/admin/courses/${courseId}`);
        const course = await res.json();
        if (res.ok) {
            const materials = course.materials || [];
            if (materials.length === 0) {
                container.innerHTML = '<p class="text-center text-muted py-3">No materials uploaded for this course.</p>';
                return;
            }
            container.innerHTML = `
                <div class="list-group list-group-flush">
                    ${materials.map(m => `
                        <div class="list-group-item d-flex justify-content-between align-items-center px-0">
                            <span><i class="bi bi-file-earmark-text me-2"></i>${m.title}</span>
                            <div>
                                <a href="${m.filePath}" target="_blank" class="btn btn-sm btn-outline-info me-1">
                                    <i class="bi bi-eye"></i>
                                </a>
                                <button class="btn btn-sm btn-outline-danger" onclick="deleteMaterial(${m.id}, ${courseId})">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>
                    `).join('')}
                </div>
            `;
        } else {
            container.innerHTML = '<p class="text-danger">Failed to load materials.</p>';
        }
    } catch (err) {
        container.innerHTML = '<p class="text-danger">Server error.</p>';
    }
}

async function deleteMaterial(materialId, courseId) {
    if (!confirm('Delete this material?')) return;
    try {
        const res = await fetch(`${API_BASE}/admin/materials/${materialId}`, { method: 'DELETE' });
        if (res.ok) {
            showNotification('Material deleted', 'success');
            loadAdminCourseMaterials(courseId);
        }
    } catch (err) {
        showNotification('Error deleting material', 'danger');
    }
}

async function viewCourseStudents(courseId, title) {
    const container = document.getElementById('courseStudentsListContainer');
    document.getElementById('courseStudentsModalTitle').textContent = `Students in ${title}`;
    container.innerHTML = '<p class="text-center text-muted">Loading...</p>';
    courseStudentsModal.show();

    try {
        const res = await fetch(`${API_BASE}/admin/courses/${courseId}`);
        const course = await res.json();
        if (res.ok) {
            const students = course.students || [];
            if (students.length === 0) {
                container.innerHTML = '<div class="alert alert-info">No students enrolled in this course yet.</div>';
                return;
            }
            container.innerHTML = `
                <div class="table-responsive">
                    <table class="table table-hover align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${students.map(s => `
                                <tr>
                                    <td><span class="badge bg-secondary">${s.studentId || '-'}</span></td>
                                    <td>${s.name}</td>
                                    <td>${s.email}</td>
                                    <td>
                                        <button class="btn btn-sm btn-outline-danger" onclick="removeStudentFromCourse(${courseId}, ${s.id}, '${title}')">
                                            <i class="bi bi-person-x me-1"></i>Remove
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            `;
        }
    } catch (err) {
        container.innerHTML = '<div class="alert alert-danger">Error loading students</div>';
    }
}

async function removeStudentFromCourse(courseId, studentId, courseTitle) {
    if (!confirm(`Are you sure you want to remove this student from ${courseTitle}?`)) return;
    try {
        const res = await fetch(`${API_BASE}/admin/courses/${courseId}/students/${studentId}`, { method: 'DELETE' });
        if (res.ok) {
            showNotification('Student removed from course', 'success');
            viewCourseStudents(courseId, courseTitle); // Refresh modal
            loadAdminCourses(); // Refresh main table
        }
    } catch (err) {
        showNotification('Error removing student', 'danger');
    }
}

function updateAdminStats() {
    fetch(`${API_BASE}/admin/courses`)
        .then(r => r.json())
        .then(courses => {
            document.getElementById('adminStatCourses').textContent = courses.length;
        })
        .catch(() => { });
}
