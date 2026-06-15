const API_BASE = '/api';
let currentUser = null;
let activeView = 'dashboard';
let cachedJobs = [];
let cachedApplications = [];

const roleMenus = {
    student: [
        ['dashboard', '🏠', 'Dashboard'],
        ['jobs', '💼', 'Browse Jobs'],
        ['applications', '📋', 'My Applications'],
        ['guidance', '🤖', 'Career Guidance'],
        ['interviews', '🗓️', 'Interviews'],
        ['profile', '👤', 'My Profile']
    ],
    recruiter: [
        ['dashboard', '🏠', 'Dashboard'],
        ['post-job', '📣', 'Post a Job'],
        ['listings', '💼', 'My Job Listings'],
        ['applicants', '👥', 'Applicants'],
        ['interviews', '🗓️', 'Interviews'],
        ['company', '🏢', 'Company Profile']
    ],
    admin: [
        ['dashboard', '📊', 'Dashboard'],
        ['users', '👥', 'Users'],
        ['notifications', '🔔', 'Notifications']
    ]
};

function escapeHtml(value = '') {
    return String(value).replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    })[char]);
}

function initials(name = 'User') {
    return name.split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase();
}

function skills(value = '') {
    return value.split(',').map(item => item.trim()).filter(Boolean);
}

function skillTags(value, tone = '') {
    return skills(value).map(skill => `<span class="tag ${tone}">${escapeHtml(skill)}</span>`).join('');
}

function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? escapeHtml(value) : date.toLocaleDateString();
}

function statusBadge(status = 'Open') {
    const key = status.toLowerCase();
    const icon = { selected: '✓', rejected: '✕', shortlisted: '★', applied: '▣', open: '●', active: '●', inactive: '●' }[key] || '●';
    return `<span class="status ${key}">${icon} ${escapeHtml(status)}</span>`;
}

function pageHeader(icon, title, subtitle, action = '') {
    return `<div class="page-header"><div><h1>${icon} ${escapeHtml(title)}</h1><p>${escapeHtml(subtitle)}</p></div>${action}</div>`;
}

function statCard(icon, value, label, tone = 'purple') {
    return `<div class="stat-card"><span class="stat-icon ${tone}">${icon}</span><div><strong>${value}</strong><small>${escapeHtml(label)}</small></div></div>`;
}

function emptyState(title, message) {
    return `<div class="empty-state"><span>📭</span><h3>${escapeHtml(title)}</h3><p>${escapeHtml(message)}</p></div>`;
}

function toast(message, type = 'success') {
    const element = document.getElementById('toast');
    if (!element) return;
    element.textContent = message;
    element.className = `toast show ${type}`;
    setTimeout(() => element.className = 'toast', 3000);
}

async function api(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
    });
    let data;
    try { data = await response.json(); } catch { data = {}; }
    if (!response.ok) throw new Error(data.message || data.error || 'Request failed');
    return data;
}

function checkAuth() {
    const stored = localStorage.getItem('currentUser');
    if (!stored) {
        window.location.href = '/index.html';
        return false;
    }
    currentUser = JSON.parse(stored);
    return true;
}

function setupShell() {
    document.getElementById('user-name').textContent = currentUser.name;
    document.getElementById('user-role').textContent = currentUser.role.toUpperCase();
    document.getElementById('user-avatar').textContent = initials(currentUser.name);
    renderNav();
}

function renderNav() {
    document.getElementById('side-nav').innerHTML = roleMenus[currentUser.role].map(([view, icon, label]) => `
        <button class="nav-item ${activeView === view ? 'active' : ''}" onclick="navigate('${view}')">
            <span>${icon}</span>${label}
        </button>`).join('');
}

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('open');
}

function navigate(view) {
    activeView = view;
    renderNav();
    document.getElementById('sidebar').classList.remove('open');
    const routes = {
        dashboard: loadDashboard,
        jobs: loadJobs,
        applications: loadApplications,
        guidance: loadGuidance,
        profile: loadProfile,
        notifications: loadNotifications,
        'post-job': loadPostJob,
        listings: loadRecruiterListings,
        applicants: loadApplicants,
        users: loadAdminUsers,
        interviews: loadInterviews,
        company: loadCompany
    };
    (routes[view] || loadDashboard)();
}

async function loadDashboard() {
    if (currentUser.role === 'student') return loadStudentDashboard();
    if (currentUser.role === 'recruiter') return loadRecruiterDashboard();
    return loadAdminDashboard();
}

async function loadStudentDashboard() {
    const container = document.getElementById('content-area');
    try {
        const [profile, applications, jobs, guidance] = await Promise.all([
            api('/student/profile'), api('/student/applications'), api('/student/jobs'), api('/student/guidance')
        ]);
        cachedApplications = applications;
        cachedJobs = jobs;
        const shortlisted = applications.filter(app => app.status === 'Shortlisted').length;
        const selected = applications.filter(app => app.status === 'Selected').length;
        container.innerHTML = `
            ${pageHeader('👋', `Welcome back, ${currentUser.name}!`, `${profile.department || 'Student'} • Year ${profile.year || ''}`, `<button class="primary-button" onclick="navigate('jobs')">Browse Jobs →</button>`)}
            <div class="stats-grid four">
                ${statCard('📋', applications.length, 'Total Applied', 'blue')}
                ${statCard('⭐', shortlisted, 'Shortlisted', 'gold')}
                ${statCard('✅', selected, 'Offers Received', 'green')}
                ${statCard('💼', jobs.length, 'Open Jobs', 'purple')}
            </div>
            <div class="dashboard-grid">
                <section class="panel">
                    <div class="panel-title"><h2>👤 Your Profile</h2><button class="text-button" onclick="navigate('profile')">Edit →</button></div>
                    <div class="profile-summary">
                        <span class="avatar large">${initials(profile.name)}</span>
                        <div><h3>${escapeHtml(profile.name)}</h3><p>${escapeHtml(profile.email)}</p></div>
                        <div class="cgpa"><strong>${profile.cgpa.toFixed(1)}</strong><small>CGPA</small></div>
                    </div>
                    <h4>SKILLS</h4><div class="tags">${skillTags(profile.skills)}</div>
                    <h4>PROFILE COMPLETENESS</h4><div class="progress"><span style="width:${profile.skills && profile.resumeSummary ? 100 : 70}%"></span></div>
                </section>
                <section class="panel">
                    <div class="panel-title"><h2>📋 Recent Applications</h2><button class="text-button" onclick="navigate('applications')">View all →</button></div>
                    ${applications.length ? applications.slice(0, 3).map(application => applicationRow(application)).join('') : emptyState('No applications yet', 'Browse jobs and submit your first application.')}
                </section>
            </div>
            <section class="panel">
                <div class="panel-title"><h2>💡 Recommended for You</h2><button class="text-button" onclick="navigate('guidance')">Career Guidance →</button></div>
                <div class="jobs-grid compact">${guidance.recommendedJobs.length ? guidance.recommendedJobs.slice(0, 3).map(job => recommendedCard(job)).join('') : emptyState('No recommendations yet', 'Add more skills to your profile to improve matches.')}</div>
            </section>`;
    } catch (error) { renderError(error); }
}

function applicationRow(application) {
    return `<div class="list-row"><span class="company-logo">${escapeHtml(application.company[0] || 'C')}</span><div><strong>${escapeHtml(application.jobTitle)}</strong><small>${escapeHtml(application.company)} • ${formatDate(application.appliedDate)}</small></div>${statusBadge(application.status)}</div>`;
}

function recommendedCard(job) {
    return `<article class="job-card">
        <div class="job-card-head"><span class="company-logo">${escapeHtml(job.company[0] || 'C')}</span><div><h3>${escapeHtml(job.title)}</h3><p>${escapeHtml(job.company)}</p></div>${statusBadge('Open')}</div>
        <div class="match-line"><span>Skill Match</span><strong>${job.matchScore}%</strong></div>
        <div class="progress ${job.matchScore < 50 ? 'warning' : ''}"><span style="width:${job.matchScore}%"></span></div>
        <button class="primary-button full" onclick="applyForJob('${job.jobId}')">View & Apply</button>
    </article>`;
}

async function loadJobs() {
    const container = document.getElementById('content-area');
    try {
        cachedJobs = await api('/student/jobs');
        container.innerHTML = `
            ${pageHeader('💼', 'Browse Jobs', `${cachedJobs.length} opportunities available`)}
            <div class="toolbar"><input id="job-search" oninput="filterJobCards()" placeholder="Search by skill, company, or role"><button class="secondary-button" onclick="document.getElementById('job-search').value='';filterJobCards()">Clear</button></div>
            <div id="jobs-container" class="jobs-grid">${cachedJobs.length ? cachedJobs.map(jobCard).join('') : emptyState('No open jobs', 'Recruiters have not posted any jobs yet.')}</div>`;
    } catch (error) { renderError(error); }
}

function jobCard(job) {
    const text = `${job.title} ${job.company} ${job.skills}`.toLowerCase();
    return `<article class="job-card" data-search="${escapeHtml(text)}">
        <div class="job-card-head"><span class="company-logo">${escapeHtml(job.company[0] || 'C')}</span><div><h3>${escapeHtml(job.title)}</h3><p>${escapeHtml(job.company)}</p></div>${statusBadge('Open')}</div>
        <div class="salary">₹${Number(job.salary).toLocaleString()} LPA</div>
        <h4>REQUIRED SKILLS</h4><div class="tags">${skillTags(job.skills)}</div>
        <p class="job-description">${escapeHtml(job.description)}</p>
        <button class="primary-button full" onclick="applyForJob('${job.jobId}')">Apply Now</button>
    </article>`;
}

function filterJobCards() {
    const value = document.getElementById('job-search').value.toLowerCase();
    document.querySelectorAll('#jobs-container .job-card').forEach(card => card.hidden = !card.dataset.search.includes(value));
}

async function applyForJob(jobId) {
    try {
        const data = await api('/student/apply', { method: 'POST', body: JSON.stringify({ jobId }) });
        toast(data.message);
    } catch (error) { toast(error.message, 'error'); }
}

async function loadApplications() {
    const container = document.getElementById('content-area');
    try {
        cachedApplications = await api('/student/applications');
        const count = status => cachedApplications.filter(app => app.status === status).length;
        container.innerHTML = `
            ${pageHeader('📋', 'My Applications', 'Track all your job applications in one place', `<button class="primary-button" onclick="navigate('jobs')">Browse More Jobs</button>`)}
            <div class="stats-grid five">
                ${statCard('📋', cachedApplications.length, 'Total', 'blue')}${statCard('📤', count('Applied'), 'Applied', 'purple')}
                ${statCard('⭐', count('Shortlisted'), 'Shortlisted', 'gold')}${statCard('✅', count('Selected'), 'Selected', 'green')}${statCard('✕', count('Rejected'), 'Rejected', 'red')}
            </div>
            <div class="filter-tabs">${['All','Applied','Shortlisted','Selected','Rejected'].map(status => `<button onclick="filterApplications('${status}', this)" class="${status === 'All' ? 'active' : ''}">${status}</button>`).join('')}</div>
            <div id="application-list" class="stack">${renderApplicationCards(cachedApplications)}</div>`;
    } catch (error) { renderError(error); }
}

function renderApplicationCards(applications) {
    return applications.length ? applications.map(app => `<article class="wide-card" data-status="${app.status}">
        <div class="job-card-head"><span class="company-logo">${escapeHtml(app.company[0] || 'C')}</span><div><h3>${escapeHtml(app.jobTitle)}</h3><p>${escapeHtml(app.company)}</p><div class="meta">${statusBadge(app.status)} <span>Applied ${formatDate(app.appliedDate)}</span></div></div></div>
        ${app.comments ? `<div class="note"><strong>Comments:</strong> ${escapeHtml(app.comments)}</div>` : ''}
    </article>`).join('') : emptyState('No applications yet', 'Browse open jobs to get started.');
}

function filterApplications(status, button) {
    document.querySelectorAll('.filter-tabs button').forEach(item => item.classList.remove('active'));
    button.classList.add('active');
    document.querySelectorAll('#application-list [data-status]').forEach(card => card.hidden = status !== 'All' && card.dataset.status !== status);
}

async function loadGuidance() {
    const container = document.getElementById('content-area');
    try {
        const [profile, guidance] = await Promise.all([api('/student/profile'), api('/student/guidance')]);
        const missing = guidance.missingSkills.replace(/^\[|\]$/g, '').split(',').map(item => item.trim()).filter(Boolean);
        container.innerHTML = `
            ${pageHeader('🤖', 'Career Guidance', 'Personalized insights based on your profile and current opportunities')}
            <div class="dashboard-grid">
                <section class="panel"><h2>🎯 Your Skills</h2><div class="tags large-tags">${skillTags(profile.skills, 'green')}</div><div class="mini-metrics"><div><strong>${profile.cgpa.toFixed(1)}</strong><small>CGPA</small></div><div><strong>${skills(profile.skills).length}</strong><small>Skills Listed</small></div><div><strong>${guidance.recommendedJobs.length}</strong><small>Job Matches</small></div></div><div class="note">${escapeHtml(guidance.academicAdvice)}</div></section>
                <section class="panel"><h2>📊 Top Skill Gaps</h2><p>Skills frequently required by open jobs that are not in your profile.</p><div class="gap-list">${missing.length ? missing.map(skill => `<div><span class="status shortlisted">LEARN</span><strong>${escapeHtml(skill)}</strong></div>`).join('') : '<p>Your profile aligns well with current jobs.</p>'}</div></section>
            </div>
            <section class="panel"><div class="panel-title"><h2>💼 Recommended Jobs for You</h2></div><div class="jobs-grid">${guidance.recommendedJobs.length ? guidance.recommendedJobs.map(recommendedCard).join('') : emptyState('No matching jobs', guidance.skillAdvice)}</div></section>`;
    } catch (error) { renderError(error); }
}

async function loadProfile() {
    const container = document.getElementById('content-area');
    try {
        const profile = await api('/student/profile');
        container.innerHTML = `
            ${pageHeader('👤', 'My Profile', 'Keep your profile updated to improve job matches')}
            <form class="profile-layout" onsubmit="saveProfile(event)">
                <div class="stack">
                    <section class="panel"><h2>🧑 Personal Information</h2><div class="form-grid"><label>Full Name<input value="${escapeHtml(profile.name)}" disabled></label><label>Email<input value="${escapeHtml(profile.email)}" disabled></label><label>Department<input value="${escapeHtml(profile.department)}" disabled></label><label>CGPA<input id="profile-cgpa" type="number" min="0" max="10" step="0.01" value="${profile.cgpa}" required></label></div></section>
                    <section class="panel"><h2>🛠️ Skills</h2><label>Enter skills separated by commas<textarea id="profile-skills" rows="4" required>${escapeHtml(profile.skills)}</textarea></label><div class="tags">${skillTags(profile.skills)}</div></section>
                    <section class="panel"><h2>📄 Resume Summary</h2><textarea id="profile-resume" rows="5" placeholder="Summarize your experience and goals">${escapeHtml(profile.resumeSummary)}</textarea><button class="primary-button" type="submit">Save Profile</button></section>
                </div>
                <aside class="panel profile-side"><span class="avatar hero">${initials(profile.name)}</span><h2>${escapeHtml(profile.name)}</h2><p>${escapeHtml(profile.department)} • Year ${profile.year}</p><div class="cgpa centered"><strong>${profile.cgpa.toFixed(1)}</strong><small>CGPA /10</small></div>${statusBadge('Active')}</aside>
            </form>`;
    } catch (error) { renderError(error); }
}

async function saveProfile(event) {
    event.preventDefault();
    try {
        const result = await api('/student/profile', { method: 'PUT', body: JSON.stringify({
            cgpa: Number(document.getElementById('profile-cgpa').value),
            skills: document.getElementById('profile-skills').value,
            resumeSummary: document.getElementById('profile-resume').value
        }) });
        toast(result.message);
        loadProfile();
    } catch (error) { toast(error.message, 'error'); }
}

async function loadRecruiterDashboard() {
    const container = document.getElementById('content-area');
    try {
        const jobs = await api('/recruiter/jobs');
        const applicantGroups = await Promise.all(jobs.map(job => api(`/recruiter/applicants?jobId=${encodeURIComponent(job.jobId)}`)));
        const applicants = applicantGroups.flat();
        container.innerHTML = `
            ${pageHeader('🏢', currentUser.name, 'Recruiter Dashboard', `<button class="primary-button" onclick="navigate('post-job')">+ Post New Job</button>`)}
            <div class="stats-grid four">${statCard('💼', jobs.length, 'Jobs Posted')}${statCard('📋', applicants.length, 'Total Applicants', 'blue')}${statCard('⭐', applicants.filter(a => a.status === 'Shortlisted').length, 'Shortlisted', 'gold')}${statCard('✅', applicants.filter(a => a.status === 'Selected').length, 'Selected', 'green')}</div>
            <div class="dashboard-grid"><section class="panel"><div class="panel-title"><h2>💼 My Job Listings</h2><button class="text-button" onclick="navigate('listings')">View all →</button></div>${jobs.length ? jobs.slice(0,4).map(job => recruiterJobRow(job)).join('') : emptyState('No jobs posted', 'Create your first job listing.')}</section>
            <section class="panel"><div class="panel-title"><h2>📋 Recent Applicants</h2><button class="text-button" onclick="navigate('applicants')">View all →</button></div>${applicants.length ? applicants.slice(0,4).map(applicantRow).join('') : emptyState('No applicants yet', 'Applicants will appear here after students apply.')}</section></div>`;
    } catch (error) { renderError(error); }
}

function recruiterJobRow(job) {
    return `<div class="list-row"><span class="company-logo">${escapeHtml(job.title[0])}</span><div><strong>${escapeHtml(job.title)}</strong><small>${escapeHtml(job.skills)}</small></div>${statusBadge(job.active ? 'Open' : 'Inactive')}</div>`;
}

function applicantRow(applicant) {
    return `<div class="list-row"><span class="company-logo">${initials(applicant.name)}</span><div><strong>${escapeHtml(applicant.name)}</strong><small>CGPA ${applicant.cgpa} • ${escapeHtml(applicant.skills)}</small></div>${statusBadge(applicant.status)}</div>`;
}

function loadPostJob() {
    document.getElementById('content-area').innerHTML = `
        ${pageHeader('📣', 'Post a New Job', 'Fill out the details to attract the right candidates')}
        <div class="profile-layout"><form class="panel" onsubmit="postJob(event)"><div class="form-grid">
            <label class="full-field">Job Title *<input id="job-title" required placeholder="e.g. Software Engineer - Java"></label>
            <label class="full-field">Job Description *<textarea id="job-description" rows="5" required placeholder="Describe the role and responsibilities"></textarea></label>
            <label>Salary (LPA) *<input id="job-salary" type="number" min="0" step="0.01" required></label>
            <label>Eligibility *<input id="job-eligibility" required placeholder="e.g. CGPA 7.0+, Final Year"></label>
            <label class="full-field">Required Skills *<input id="job-skills" required placeholder="Java, Spring Boot, SQL, REST APIs"></label>
        </div><button class="primary-button" type="submit">Publish Job</button></form>
        <aside class="panel"><h2>💡 Tips for a great listing</h2><ul class="tips"><li>Be specific about required skills</li><li>Set a realistic eligibility cutoff</li><li>Write a clear role description</li><li>Include an accurate salary range</li></ul></aside></div>`;
}

async function postJob(event) {
    event.preventDefault();
    try {
        const result = await api('/recruiter/postjob', { method: 'POST', body: JSON.stringify({
            title: document.getElementById('job-title').value,
            description: document.getElementById('job-description').value,
            skills: document.getElementById('job-skills').value,
            salary: Number(document.getElementById('job-salary').value),
            eligibility: document.getElementById('job-eligibility').value
        }) });
        toast(result.message);
        navigate('listings');
    } catch (error) { toast(error.message, 'error'); }
}

async function loadRecruiterListings() {
    const container = document.getElementById('content-area');
    try {
        const jobs = await api('/recruiter/jobs');
        container.innerHTML = `${pageHeader('💼', 'My Job Listings', `${jobs.length} jobs posted`, `<button class="primary-button" onclick="navigate('post-job')">+ Post New Job</button>`)}
            <div class="stack">${jobs.length ? jobs.map(job => `<article class="wide-card"><div class="job-card-head"><span class="company-logo">${escapeHtml(job.title[0])}</span><div><h3>${escapeHtml(job.title)}</h3><p>₹${Number(job.salary).toLocaleString()} LPA</p><div class="meta">${statusBadge(job.active ? 'Open' : 'Inactive')}</div><div class="tags">${skillTags(job.skills)}</div></div><button class="secondary-button" onclick="navigate('applicants')">👥 Applicants</button></div></article>`).join('') : emptyState('No jobs posted', 'Post a job to start receiving applications.')}</div>`;
    } catch (error) { renderError(error); }
}

async function loadApplicants() {
    const container = document.getElementById('content-area');
    try {
        const jobs = await api('/recruiter/jobs');
        const groups = await Promise.all(jobs.map(async job => (await api(`/recruiter/applicants?jobId=${encodeURIComponent(job.jobId)}`)).map(app => ({...app, jobTitle: job.title}))));
        const applicants = groups.flat();
        container.innerHTML = `${pageHeader('👥', 'Applicants', `${applicants.length} application(s)`)}
            <div class="stack">${applicants.length ? applicants.map(app => `<article class="wide-card applicant-card"><div class="job-card-head"><span class="company-logo">${initials(app.name)}</span><div><h3>${escapeHtml(app.name)}</h3><p>CGPA ${app.cgpa} • ${escapeHtml(app.email)}</p><strong>${escapeHtml(app.jobTitle)}</strong><div class="tags">${skillTags(app.skills)}</div></div><div class="actions">${statusBadge(app.status)}<button class="success-button" onclick="updateApplicant('${app.applicationId}','Selected')">✓ Select</button><button class="danger-button" onclick="updateApplicant('${app.applicationId}','Rejected')">✕ Reject</button><button class="secondary-button" onclick="updateApplicant('${app.applicationId}','Shortlisted')">★ Shortlist</button></div></div></article>`).join('') : emptyState('No applicants yet', 'Applications will appear here after students apply.')}</div>`;
    } catch (error) { renderError(error); }
}

async function updateApplicant(applicationId, status) {
    try {
        const result = await api('/recruiter/applicants', { method: 'PUT', body: JSON.stringify({ applicationId, status, comments: `Application marked ${status} by recruiter.` }) });
        toast(result.message);
        loadApplicants();
    } catch (error) { toast(error.message, 'error'); }
}

async function loadAdminDashboard() {
    const container = document.getElementById('content-area');
    try {
        const stats = await api('/admin/statistics');
        container.innerHTML = `${pageHeader('📊', 'Admin Dashboard', 'Monitor CampusConnect activity and outcomes')}
            <div class="stats-grid four">${statCard('🎓', stats.totalStudents, 'Students', 'blue')}${statCard('🏢', stats.totalRecruiters, 'Recruiters')}${statCard('💼', stats.totalJobs, 'Jobs', 'gold')}${statCard('📋', stats.totalApplications, 'Applications', 'green')}</div>
            <div class="dashboard-grid"><section class="panel"><h2>Placement Overview</h2><div class="mini-metrics"><div><strong>${stats.placedStudents}</strong><small>Placed Students</small></div><div><strong>${stats.placementRate.toFixed(1)}%</strong><small>Placement Rate</small></div><div><strong>₹${stats.averageCTC.toFixed(1)}</strong><small>Average CTC</small></div></div></section>
            <section class="panel"><h2>📣 Broadcast Message</h2><textarea id="broadcast-message" rows="4" placeholder="Write an announcement for all users"></textarea><button class="primary-button" onclick="sendBroadcast()">Send Broadcast</button></section></div>`;
    } catch (error) { renderError(error); }
}

async function loadAdminUsers() {
    const container = document.getElementById('content-area');
    try {
        const users = await api('/admin/users');
        container.innerHTML = `${pageHeader('👥', 'Users', `${users.length} registered accounts`)}<section class="panel users-table"><table><thead><tr><th>User</th><th>Email</th><th>Role</th><th>Status</th></tr></thead><tbody>${users.map(user => `<tr><td><strong>${escapeHtml(user.name)}</strong></td><td>${escapeHtml(user.email)}</td><td><span class="tag">${escapeHtml(user.role)}</span></td><td>${statusBadge(user.active ? 'Active' : 'Inactive')}</td></tr>`).join('')}</tbody></table></section>`;
    } catch (error) { renderError(error); }
}

async function sendBroadcast() {
    const input = document.getElementById('broadcast-message');
    if (!input.value.trim()) return toast('Enter a message first.', 'error');
    try {
        const result = await api('/admin/broadcast', { method: 'POST', body: JSON.stringify({ message: input.value.trim() }) });
        input.value = '';
        toast(result.message);
    } catch (error) { toast(error.message, 'error'); }
}

async function loadNotifications() {
    const container = document.getElementById('content-area');
    try {
        const notifications = await api('/notifications');
        container.innerHTML = `${pageHeader('🔔', 'Notifications', `${notifications.filter(item => !item.read).length} unread`)}<div class="stack">${notifications.length ? notifications.map(item => `<article class="wide-card ${item.read ? '' : 'unread'}"><div class="panel-title"><div><h3>${escapeHtml(item.message)}</h3><p>${formatDate(item.date)} • ${escapeHtml(item.type)}</p></div>${item.read ? statusBadge('Read') : `<button class="secondary-button" onclick="markAsRead('${item.id}')">Mark as read</button>`}</div></article>`).join('') : emptyState('No notifications', 'Updates and announcements will appear here.')}</div>`;
    } catch (error) { renderError(error); }
}

async function markAsRead(id) {
    try { await api('/notifications', { method: 'PUT', body: JSON.stringify({ notificationId: id }) }); loadNotifications(); }
    catch (error) { toast(error.message, 'error'); }
}

function loadInterviews() {
    document.getElementById('content-area').innerHTML = `${pageHeader('🗓️', 'Interviews', 'Manage upcoming interview schedules')}${emptyState('No interviews scheduled', 'Scheduled interviews will appear here.')}`;
}

function loadCompany() {
    document.getElementById('content-area').innerHTML = `${pageHeader('🏢', 'Company Profile', 'Your recruiter account overview')}<section class="panel profile-side"><span class="avatar hero">${initials(currentUser.name)}</span><h2>${escapeHtml(currentUser.name)}</h2><p>Recruiter account</p>${statusBadge('Active')}</section>`;
}

function renderError(error) {
    document.getElementById('content-area').innerHTML = `<div class="error-card"><h2>Something went wrong</h2><p>${escapeHtml(error.message)}</p><button class="primary-button" onclick="navigate('${activeView}')">Try Again</button></div>`;
}

async function logout() {
    try { await api('/logout', { method: 'POST' }); } catch {}
    localStorage.removeItem('currentUser');
    window.location.href = '/index.html';
}

function showTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(button => button.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(form => form.classList.remove('active'));
    const index = tab === 'login' ? 0 : 1;
    document.querySelectorAll('.tab-btn')[index].classList.add('active');
    document.getElementById(`${tab}-form`).classList.add('active');
}

function toggleStudentFields() {
    const student = document.getElementById('reg-role').value === 'student';
    document.getElementById('student-fields').style.display = student ? 'block' : 'none';
    document.getElementById('recruiter-fields').style.display = student ? 'none' : 'block';
}

async function handleLogin(event) {
    event.preventDefault();
    try {
        const data = await api('/login', { method: 'POST', body: JSON.stringify({ email: document.getElementById('login-email').value, password: document.getElementById('login-password').value }) });
        currentUser = { id: data.userId, name: data.name, role: data.role };
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        window.location.href = '/dashboard.html';
    } catch (error) { alert(error.message); }
}

async function handleRegister(event) {
    event.preventDefault();
    const role = document.getElementById('reg-role').value;
    const body = { role, name: document.getElementById('reg-name').value, email: document.getElementById('reg-email').value, password: document.getElementById('reg-password').value };
    if (role === 'student') Object.assign(body, { rollNumber: document.getElementById('reg-roll').value, department: document.getElementById('reg-dept').value, cgpa: Number(document.getElementById('reg-cgpa').value), skills: document.getElementById('reg-skills').value });
    else Object.assign(body, { company: document.getElementById('reg-company').value, companyDesc: document.getElementById('reg-company-desc').value });
    try { const data = await api('/register', { method: 'POST', body: JSON.stringify(body) }); alert(data.message); showTab('login'); }
    catch (error) { alert(error.message); }
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.body.classList.contains('dashboard-body') && checkAuth()) {
        setupShell();
        const pendingView = sessionStorage.getItem('pendingView');
        sessionStorage.removeItem('pendingView');
        pendingView ? navigate(pendingView) : loadDashboard();
    } else if (!window.location.pathname.includes('index.html') && window.location.pathname !== '/') {
        const legacyRoutes = {
            '/jobs.html': 'jobs',
            '/applications.html': 'applications',
            '/notifications.html': 'notifications',
            '/admin.html': 'dashboard'
        };
        if (legacyRoutes[window.location.pathname]) {
            sessionStorage.setItem('pendingView', legacyRoutes[window.location.pathname]);
            window.location.href = '/dashboard.html';
        }
    }
});
