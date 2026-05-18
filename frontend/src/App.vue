<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">T</div>
        <div>
          <p class="brand-kicker">TASK PILOT</p>
          <h1>今日清单</h1>
        </div>
      </div>

      <section v-if="!isAuthenticated" class="sidebar-panel identity-panel">
        <div class="panel-head">
          <div>
            <h2>欢迎回来</h2>
            <p>{{ authHint }}</p>
          </div>
        </div>

        <form id="loginForm" class="form" @submit.prevent="handleLogin">
          <label for="username" class="visually-hidden">用户名</label>
          <input
            id="username"
            v-model.trim="loginForm.username"
            class="input"
            type="text"
            placeholder="用户名"
            :disabled="isAuthenticated"
            required
          >
          <label for="password" class="visually-hidden">密码</label>
          <input
            id="password"
            v-model="loginForm.password"
            class="input"
            type="password"
            placeholder="密码"
            :disabled="isAuthenticated"
            required
          >
          <div id="loginError" class="field-error" aria-live="polite">{{ loginError }}</div>
          <button
            id="loginButton"
            class="button button-primary button-block"
            type="submit"
            :disabled="loading.login"
          >
            {{ loading.login ? "登录中..." : "登录" }}
          </button>
        </form>
      </section>

      <section v-if="!isAuthenticated" id="registerPanel" class="sidebar-panel register-panel">
        <div class="panel-head compact">
          <div>
            <h2>创建账号</h2>
            <p>注册后就能拥有自己的任务列表。</p>
          </div>
        </div>

        <form id="registerForm" class="form" @submit.prevent="handleRegister">
          <label for="registerUsername" class="visually-hidden">新用户名</label>
          <input
            id="registerUsername"
            v-model.trim="registerForm.username"
            class="input"
            type="text"
            maxlength="50"
            placeholder="新用户名"
            required
          >
          <label for="registerPassword" class="visually-hidden">密码</label>
          <input
            id="registerPassword"
            v-model="registerForm.password"
            class="input"
            type="password"
            maxlength="100"
            placeholder="至少 6 位密码"
            required
          >
          <div id="registerError" class="field-error" aria-live="polite">{{ registerError }}</div>
          <button
            id="registerButton"
            class="button button-secondary button-block"
            type="submit"
            :disabled="loading.register"
          >
            {{ loading.register ? "创建中..." : "创建账号" }}
          </button>
        </form>
      </section>

      <section v-if="isAuthenticated" id="currentUserPanel" class="sidebar-panel current-user-panel">
        <div class="panel-head compact">
          <div>
            <h2>我的账号</h2>
            <p id="userPanelHint">{{ isAdmin ? "你当前拥有管理员权限。" : "这里只显示你的个人工作区。" }}</p>
          </div>
        </div>

        <div id="currentUserCard" class="identity-details">
          <template v-if="currentUser">
            <div class="identity-tags">
              <span class="role-tag" :class="{ admin: currentUser.role === 'ADMIN' }">{{ currentUser.role === "ADMIN" ? "管理员" : "普通用户" }}</span>
              <span class="user-state" :class="{ inactive: !currentUser.enabled }">{{ currentUser.enabled ? "已启用" : "已禁用" }}</span>
            </div>
            <div class="identity-name">{{ currentUser.username }}</div>
            <div class="identity-meta">账号编号 #{{ currentUser.id }}</div>
          </template>
        </div>

        <div class="account-actions">
          <button
            v-if="isAdmin"
            id="openAdminPanelButton"
            class="button button-secondary button-block admin-entry"
            type="button"
            @click="openAdminPanel"
          >
            打开用户管理
          </button>
          <button
            id="logoutButton"
            class="button logout-link button-block"
            type="button"
            @click="handleLogout"
          >
            退出登录
          </button>
        </div>
      </section>
    </aside>

    <main class="workspace">
      <header class="workspace-hero">
        <div class="hero-copy">
          <p class="hero-kicker">{{ todayLabel }}</p>
          <h2>{{ heroTitle }}</h2>
          <p class="hero-summary">{{ heroSummary }}</p>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <span>全部任务</span>
            <strong>{{ todos.length }}</strong>
          </div>
          <div class="hero-stat accent">
            <span>已完成</span>
            <strong>{{ doneCount }}</strong>
          </div>
          <div class="hero-stat">
            <span>待处理</span>
            <strong>{{ openCount }}</strong>
          </div>
        </div>
      </header>

      <section class="workspace-grid">
        <section class="main-column">
          <section class="composer-panel">
            <div class="panel-head">
              <div>
                <h3>新任务</h3>
                <p>把接下来要做的事写下来，保存后会立刻进入你的列表。</p>
              </div>
            </div>

            <form id="todoForm" class="composer-form" @submit.prevent="handleCreateTodo">
              <label for="title" class="visually-hidden">任务标题</label>
              <input
                id="title"
                v-model="todoForm.title"
                name="title"
                class="input input-large"
                type="text"
                maxlength="120"
                placeholder="例如：整理会议纪要、回访客户、更新接口文档"
                :disabled="!isAuthenticated"
                required
              >
              <label for="priority" class="visually-hidden">优先级</label>
              <select
                id="priority"
                v-model="todoForm.priority"
                class="input priority-select"
                :disabled="!isAuthenticated"
              >
                <option value="HIGH">高优先级</option>
                <option value="MEDIUM">中优先级</option>
                <option value="LOW">低优先级</option>
              </select>
              <button
                id="submitButton"
                class="button button-primary"
                type="submit"
                :disabled="!isAuthenticated || loading.createTodo"
              >
                {{ loading.createTodo ? "保存中..." : "添加任务" }}
              </button>
            </form>
            <div id="titleError" class="field-error" aria-live="polite">{{ titleError }}</div>
            <div id="statusMessage" class="status" :class="status.type" aria-live="polite">{{ status.message }}</div>
          </section>

          <section class="tasks-panel">
            <div class="panel-head">
              <div>
                <h3>我的任务</h3>
                <p>{{ listHint }}</p>
              </div>
              <div class="task-tools">
                <div class="filter-group" role="tablist" aria-label="任务筛选">
                  <button class="filter-chip" :class="{ active: currentFilter === 'all' }" type="button" data-filter="all" @click="currentFilter = 'all'">全部</button>
                  <button class="filter-chip" :class="{ active: currentFilter === 'open' }" type="button" data-filter="open" @click="currentFilter = 'open'">进行中</button>
                  <button class="filter-chip" :class="{ active: currentFilter === 'done' }" type="button" data-filter="done" @click="currentFilter = 'done'">已完成</button>
                </div>
                <button
                  id="refreshButton"
                  class="button button-secondary"
                  type="button"
                  :disabled="!isAuthenticated || loading.todos"
                  @click="refreshTodos"
                >
                  {{ loading.todos ? "刷新中..." : "刷新" }}
                </button>
              </div>
            </div>

            <div class="search-row">
              <label for="taskSearch" class="visually-hidden">搜索任务</label>
              <input
                id="taskSearch"
                v-model.trim="searchQuery"
                class="input"
                type="search"
                placeholder="搜索任务内容"
                :disabled="!isAuthenticated"
              >
              <button
                v-if="searchQuery"
                id="clearSearchButton"
                class="button ghost"
                type="button"
                @click="searchQuery = ''"
              >
                清空
              </button>
            </div>

            <div id="filterNote" class="toolbar-note">{{ filterNote }}</div>

            <div v-if="!isAuthenticated" class="empty-state login-empty">
              <strong>登录后开始安排今天的工作</strong>
              <span>你的任务只对你自己可见，登录后这里会显示你的清单。</span>
            </div>

            <div v-else-if="filteredTodos.length === 0 && searchQuery" class="empty-state">
              <strong>没有找到匹配的任务</strong>
              <span>换一个关键词试试，或者清空搜索查看全部任务。</span>
            </div>

            <div v-else-if="visibleTodos.length === 0" class="empty-state">
              <strong>还没有任务</strong>
              <span>先添加第一条，今天的安排就从这里开始。</span>
            </div>

            <div v-else id="todoList" class="todo-list">
              <section v-for="section in todoSections" :key="section.key" class="todo-section">
                <div class="todo-section-head">
                  <h4>{{ section.title }}</h4>
                  <span>{{ section.items.length }}</span>
                </div>

                <article v-for="todo in section.items" :key="todo.id" class="todo-item">
                  <div class="todo-main">
                    <div class="todo-topline">
                      <span class="todo-tag" :class="{ done: todo.done }">{{ todo.done ? "已完成" : "进行中" }}</span>
                      <span class="priority-pill" :class="priorityClass(todo.priority)">{{ priorityLabel(todo.priority) }}</span>
                      <span class="todo-id">#{{ todo.id }}</span>
                    </div>

                    <template v-if="editingId === todo.id">
                      <div class="todo-editor">
                        <input
                          :id="`edit-input-${todo.id}`"
                          v-model="editingTitle"
                          class="inline-input"
                          type="text"
                          maxlength="120"
                        >
                        <select
                          :id="`edit-priority-${todo.id}`"
                          v-model="editingPriority"
                          class="input priority-select"
                        >
                          <option value="HIGH">高优先级</option>
                          <option value="MEDIUM">中优先级</option>
                          <option value="LOW">低优先级</option>
                        </select>
                        <div class="field-error" aria-live="polite">{{ inlineEditError }}</div>
                        <div class="todo-editor-actions">
                          <el-button
                            class="editor-save-button"
                            type="primary"
                            size="small"
                            data-action="save"
                            :loading="loading.actionId === todo.id"
                            @click="saveTodo(todo.id)"
                          >
                            保存
                          </el-button>
                          <el-button
                            class="editor-cancel-button"
                            size="small"
                            data-action="cancel"
                            :disabled="loading.actionId === todo.id"
                            @click="cancelEdit"
                          >
                            取消
                          </el-button>
                        </div>
                      </div>
                    </template>
                    <template v-else>
                      <h4 class="todo-title" :class="{ done: todo.done }">{{ todo.title }}</h4>
                    </template>
                  </div>

                  <div class="todo-actions">
                    <button class="button ghost" type="button" data-action="edit" @click="startEdit(todo)">编辑</button>
                    <button class="button ghost" type="button" data-action="done" :disabled="todo.done || loading.actionId === todo.id" @click="markDone(todo.id)">完成</button>
                    <button class="button ghost danger" type="button" data-action="delete" :disabled="loading.actionId === todo.id" @click="promptDelete(todo)">删除</button>
                  </div>
                </article>
              </section>
            </div>
          </section>
        </section>

        <aside class="side-column">
          <section class="inspector-panel">
            <div class="panel-head compact">
              <div>
                <h3>今日进度</h3>
                <p>{{ progressSummary }}</p>
              </div>
            </div>

            <div class="progress-ring">
              <div class="progress-number">{{ progressPercent }}%</div>
              <div class="progress-label">完成率</div>
            </div>

            <div class="summary-list">
              <div class="summary-item">
                <span>当前重点</span>
                <strong>{{ focusLabel }}</strong>
              </div>
              <div class="summary-item">
                <span>进行中</span>
                <strong>{{ openCount }}</strong>
              </div>
              <div class="summary-item">
                <span>已完成</span>
                <strong>{{ doneCount }}</strong>
              </div>
            </div>
          </section>

          <section v-if="isAuthenticated && hasRecentCompleted" class="inspector-panel">
            <div class="panel-head compact">
              <div>
                <h3>最近完成</h3>
                <p>刚刚处理完的事情会暂时留在这里，方便你回看进度。</p>
              </div>
            </div>

            <div class="recent-list">
              <article v-for="todo in recentCompletedTodos" :key="`recent-${todo.id}`" class="recent-item">
                <span class="recent-badge">已完成</span>
                <strong>{{ todo.title }}</strong>
                <span class="recent-meta">任务 #{{ todo.id }}</span>
              </article>
            </div>
          </section>

        </aside>
      </section>
    </main>

    <div
      v-if="isAdmin && adminPanelOpen"
      id="adminUserPanel"
      class="admin-overlay"
      @click.self="closeAdminPanel"
    >
      <section class="admin-sheet" role="dialog" aria-modal="true" aria-labelledby="adminPanelTitle">
        <div class="panel-head">
          <div>
            <h3 id="adminPanelTitle">用户管理</h3>
            <p>启用或禁用账号，保持系统可控。</p>
          </div>
          <div class="admin-sheet-actions">
            <button
              id="loadUsersButton"
              class="button button-secondary"
              type="button"
              :disabled="loading.users"
              @click="refreshUsers"
            >
              {{ loading.users ? "加载中..." : "刷新用户" }}
            </button>
            <button
              id="closeAdminPanelButton"
              class="button ghost"
              type="button"
              @click="closeAdminPanel"
            >
              关闭
            </button>
          </div>
        </div>

        <div id="userList" class="user-list">
          <div v-if="users.length === 0" class="empty-state compact-empty">
            <strong>暂无可显示的用户</strong>
          </div>
          <article v-for="user in users" :key="user.id" class="user-row">
            <div class="user-row-main">
              <div class="identity-tags">
                <span class="role-tag" :class="{ admin: user.role === 'ADMIN' }">{{ user.role === "ADMIN" ? "管理员" : "普通用户" }}</span>
                <span class="user-state" :class="{ inactive: !user.enabled }">{{ user.enabled ? "启用中" : "已禁用" }}</span>
              </div>
              <div class="identity-name">{{ user.username }}</div>
              <div class="identity-meta">{{ user.username === currentUser?.username ? "当前账号" : `账号编号 #${user.id}` }}</div>
            </div>
            <button
              class="button ghost"
              type="button"
              :data-user-action="user.enabled ? 'disable' : 'enable'"
              :disabled="user.username === currentUser?.username || loading.userActionId === user.id"
              @click="toggleUserEnabled(user)"
            >
              {{ user.enabled ? "禁用" : "启用" }}
            </button>
          </article>
        </div>
      </section>
    </div>

    <div
      v-if="pendingDeleteTodo"
      id="deleteConfirmDialog"
      class="admin-overlay"
      @click.self="cancelDelete"
    >
      <section class="confirm-sheet" role="dialog" aria-modal="true" aria-labelledby="deleteConfirmTitle">
        <div class="panel-head compact">
          <div>
            <h3 id="deleteConfirmTitle">确认删除任务</h3>
            <p>删除后将无法恢复，请确认这条任务已经不再需要。</p>
          </div>
        </div>

        <div class="confirm-body">
          <strong>{{ pendingDeleteTodo.title }}</strong>
          <span>任务 #{{ pendingDeleteTodo.id }}</span>
        </div>

        <div class="confirm-actions">
          <button
            id="cancelDeleteButton"
            class="button ghost"
            type="button"
            :disabled="loading.actionId === pendingDeleteTodo.id"
            @click="cancelDelete"
          >
            取消
          </button>
          <button
            id="confirmDeleteButton"
            class="button button-danger"
            type="button"
            :disabled="loading.actionId === pendingDeleteTodo.id"
            @click="confirmDelete"
          >
            {{ loading.actionId === pendingDeleteTodo.id ? "删除中..." : "确认删除" }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { nextTick } from "vue";

export default {
  data() {
    return {
      tokenKey: "todo_jwt_token",
      token: localStorage.getItem("todo_jwt_token") || "",
      loginForm: {
        username: "",
        password: ""
      },
      registerForm: {
        username: "",
        password: ""
      },
      todoForm: {
        title: "",
        priority: "MEDIUM"
      },
      currentFilter: "all",
      searchQuery: "",
      todos: [],
      users: [],
      currentUser: null,
      adminPanelOpen: false,
      pendingDeleteTodo: null,
      editingId: null,
      editingTitle: "",
      editingPriority: "MEDIUM",
      inlineEditError: "",
      titleError: "",
      loginError: "",
      registerError: "",
      status: {
        message: "",
        type: ""
      },
      loading: {
        login: false,
        register: false,
        todos: false,
        users: false,
        createTodo: false,
        actionId: null,
        userActionId: null
      }
    };
  },
  computed: {
    isAuthenticated() {
      return Boolean(this.token);
    },
    isAdmin() {
      return this.currentUser?.role === "ADMIN";
    },
    visibleTodos() {
      if (this.currentFilter === "open") {
        return this.todos.filter((todo) => !todo.done);
      }
      if (this.currentFilter === "done") {
        return this.todos.filter((todo) => todo.done);
      }
      return this.todos;
    },
    filteredTodos() {
      const keyword = this.searchQuery.trim().toLowerCase();
      if (!keyword) {
        return this.visibleTodos;
      }
      return this.visibleTodos.filter((todo) => todo.title.toLowerCase().includes(keyword));
    },
    sortedFilteredTodos() {
      return [...this.filteredTodos].sort((left, right) => {
        const rankDifference = this.getPriorityRank(left.priority) - this.getPriorityRank(right.priority);
        if (rankDifference !== 0) {
          return rankDifference;
        }
        return left.id - right.id;
      });
    },
    todoSections() {
      if (this.currentFilter === "open") {
        return this.sortedFilteredTodos.length > 0
          ? [{ key: "open", title: "进行中", items: this.sortedFilteredTodos }]
          : [];
      }
      if (this.currentFilter === "done") {
        return this.sortedFilteredTodos.length > 0
          ? [{ key: "done", title: "已完成", items: this.sortedFilteredTodos }]
          : [];
      }
      const openTodos = this.sortedFilteredTodos.filter((todo) => !todo.done);
      const doneTodos = this.sortedFilteredTodos.filter((todo) => todo.done);
      return [
        openTodos.length > 0 ? { key: "open", title: "进行中", items: openTodos } : null,
        doneTodos.length > 0 ? { key: "done", title: "已完成", items: doneTodos } : null
      ].filter(Boolean);
    },
    doneCount() {
      return this.todos.filter((todo) => todo.done).length;
    },
    recentCompletedTodos() {
      return this.todos.filter((todo) => todo.done).slice(0, 3);
    },
    openCount() {
      return this.todos.filter((todo) => !todo.done).length;
    },
    progressPercent() {
      if (this.todos.length === 0) {
        return 0;
      }
      return Math.round((this.doneCount / this.todos.length) * 100);
    },
    filterNote() {
      if (this.searchQuery.trim()) {
        return `当前匹配到 ${this.filteredTodos.length} 条任务`;
      }
      if (this.currentFilter === "open") {
        return `当前显示 ${this.visibleTodos.length} 条进行中的任务`;
      }
      if (this.currentFilter === "done") {
        return `当前显示 ${this.visibleTodos.length} 条已完成任务`;
      }
      return `当前共有 ${this.visibleTodos.length} 条任务`;
    },
    authHint() {
      return this.isAuthenticated
        ? "已登录，开始安排和处理你的任务。"
        : "登录后即可查看和管理自己的任务。";
    },
    todayLabel() {
      const today = new Date();
      return today.toLocaleDateString("zh-CN", {
        month: "long",
        day: "numeric",
        weekday: "long"
      });
    },
    heroTitle() {
      if (!this.isAuthenticated) {
        return "把今天要做的事安排清楚";
      }
      return this.openCount > 0
        ? `${this.displayName}，先把最重要的几件事做完`
        : `${this.displayName}，今天的任务已经清空了`;
    },
    heroSummary() {
      if (!this.isAuthenticated) {
        return "登录后添加任务、整理进度，所有任务都会和你的账号一一对应。";
      }
      if (this.todos.length === 0) {
        return "现在是一个很好的开始时间，先写下今天第一条任务。";
      }
      if (this.openCount === 0) {
        return "列表里的任务都已经完成了，可以放心收工或开始新的安排。";
      }
      return `你还有 ${this.openCount} 条任务待处理，继续推进就好。`;
    },
    displayName() {
      return this.currentUser?.username || "今天";
    },
    listHint() {
      return this.isAuthenticated
        ? "这里只显示你自己的任务，其他人的内容不会出现在这里。"
        : "登录后这里会显示你的个人任务列表。";
    },
    progressSummary() {
      if (!this.isAuthenticated) {
        return "登录后这里会显示你的完成情况。";
      }
      if (this.todos.length === 0) {
        return "还没有任务，先添加第一条开始今天的安排。";
      }
      if (this.openCount === 0) {
        return "所有任务都完成了，今天进展不错。";
      }
      return `已经完成 ${this.doneCount} 条，还剩 ${this.openCount} 条。`;
    },
    focusLabel() {
      if (!this.isAuthenticated) {
        return "等待登录";
      }
      const nextTodo = [...this.todos]
        .filter((todo) => !todo.done)
        .sort((left, right) => this.getPriorityRank(left.priority) - this.getPriorityRank(right.priority))[0];
      return nextTodo ? nextTodo.title : "全部完成";
    },
    hasRecentCompleted() {
      return this.recentCompletedTodos.length > 0;
    }
  },
  methods: {
    getPriorityRank(priority) {
      if (priority === "HIGH") {
        return 0;
      }
      if (priority === "LOW") {
        return 2;
      }
      return 1;
    },
    priorityLabel(priority) {
      if (priority === "HIGH") {
        return "高优先级";
      }
      if (priority === "LOW") {
        return "低优先级";
      }
      return "中优先级";
    },
    priorityClass(priority) {
      if (priority === "HIGH") {
        return "high";
      }
      if (priority === "LOW") {
        return "low";
      }
      return "medium";
    },
    setStatus(message, type = "") {
      this.status.message = message;
      this.status.type = type;
    },
    clearAuthState() {
      this.token = "";
      localStorage.removeItem(this.tokenKey);
      this.currentUser = null;
      this.adminPanelOpen = false;
      this.pendingDeleteTodo = null;
      this.users = [];
      this.todos = [];
      this.editingId = null;
      this.editingTitle = "";
      this.editingPriority = "MEDIUM";
      this.inlineEditError = "";
      this.todoForm.title = "";
      this.todoForm.priority = "MEDIUM";
    },
    validateTitle(title) {
      const value = (title || "").trim();
      if (!value) {
        return "任务内容不能为空。";
      }
      if (value.length > 120) {
        return "任务内容不能超过 120 个字符。";
      }
      return "";
    },
    validateRegisterForm(username, password) {
      if (!username || !username.trim()) {
        return "用户名不能为空。";
      }
      if (username.trim().length > 50) {
        return "用户名不能超过 50 个字符。";
      }
      if (!password || !password.trim()) {
        return "密码不能为空。";
      }
      if (password.length < 6) {
        return "密码至少需要 6 位。";
      }
      return "";
    },
    translateError(errorPayload, fallbackMessage) {
      if (!errorPayload) {
        return fallbackMessage;
      }
      if (errorPayload.code === "VALIDATION_ERROR" && Array.isArray(errorPayload.errors) && errorPayload.errors.length > 0) {
        const firstError = errorPayload.errors[0];
        if (firstError.includes("title")) {
          return firstError.includes("blank") ? "任务内容不能为空。" : "任务内容不能超过 120 个字符。";
        }
        if (firstError.includes("priority")) {
          return "优先级只能是高、中、低中的一种。";
        }
        if (firstError.includes("username")) {
          return firstError.includes("blank") ? "用户名不能为空。" : "用户名不能超过 50 个字符。";
        }
        if (firstError.includes("password")) {
          return firstError.includes("blank") ? "密码不能为空。" : "密码长度需要在 6 到 100 个字符之间。";
        }
        return "输入内容不符合要求。";
      }
      if (errorPayload.code === "TODO_NOT_FOUND") {
        return "这条任务不存在，可能已经被删除。";
      }
      if (errorPayload.code === "USER_ALREADY_EXISTS") {
        return "该用户名已存在，请换一个用户名。";
      }
      if (errorPayload.code === "USER_OPERATION_NOT_ALLOWED") {
        return errorPayload.message || "当前操作不被允许。";
      }
      if (errorPayload.code === "UNAUTHORIZED") {
        return errorPayload.message || "账号或密码不正确，请重新输入。";
      }
      if (typeof errorPayload.message === "string" && errorPayload.message.trim()) {
        return errorPayload.message;
      }
      return fallbackMessage;
    },
    async ensureSuccess(response, fallbackMessage) {
      if (response.ok) {
        if (response.status === 204) {
          return null;
        }
        return response.json();
      }
      let errorPayload = null;
      try {
        errorPayload = await response.json();
      } catch {
        errorPayload = null;
      }
      throw new Error(this.translateError(errorPayload, fallbackMessage));
    },
    async apiFetch(url, options = {}, fallbackMessage = "请求失败，请稍后再试。") {
      const headers = new Headers(options.headers || {});
      if (this.token) {
        headers.set("Authorization", `Bearer ${this.token}`);
      }
      const response = await fetch(url, { ...options, headers });
      return this.ensureSuccess(response, fallbackMessage);
    },
    async loadCurrentUser() {
      const payload = await this.apiFetch("/api/users/me", {}, "加载账号信息失败，请稍后再试。");
      this.currentUser = payload.data;
    },
    async loadTodos() {
      if (!this.isAuthenticated) {
        this.todos = [];
        return;
      }
      this.loading.todos = true;
      try {
        const payload = await this.apiFetch("/api/todos", {}, "加载任务失败，请稍后再试。");
        this.todos = Array.isArray(payload.data) ? payload.data : [];
      } finally {
        this.loading.todos = false;
      }
    },
    async loadUsers() {
      if (!this.isAdmin) {
        this.users = [];
        return;
      }
      this.loading.users = true;
      try {
        const payload = await this.apiFetch("/api/users", {}, "加载用户失败，请稍后再试。");
        this.users = Array.isArray(payload.data) ? payload.data : [];
      } finally {
        this.loading.users = false;
      }
    },
    async afterLoginSuccess() {
      await this.loadCurrentUser();
      await this.loadTodos();
      await this.loadUsers();
    },
    async handleLogin() {
      this.loginError = "";
      this.loading.login = true;
      try {
        const response = await fetch("/api/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(this.loginForm)
        });
        const payload = await this.ensureSuccess(response, "登录失败，请稍后再试。");
        this.token = payload.data.token;
        localStorage.setItem(this.tokenKey, this.token);
        await this.afterLoginSuccess();
        this.setStatus("登录成功。", "success");
      } catch (error) {
        this.loginError = error.message;
        this.setStatus(error.message, "error");
      } finally {
        this.loading.login = false;
      }
    },
    async handleRegister() {
      const validationMessage = this.validateRegisterForm(this.registerForm.username, this.registerForm.password);
      if (validationMessage) {
        this.registerError = validationMessage;
        return;
      }
      this.loading.register = true;
      this.registerError = "";
      try {
        const response = await fetch("/api/auth/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(this.registerForm)
        });
        await this.ensureSuccess(response, "创建账号失败，请稍后再试。");
        const username = this.registerForm.username;
        this.registerForm.username = "";
        this.registerForm.password = "";
        this.setStatus(`账号 ${username} 已创建，请登录。`, "success");
      } catch (error) {
        this.registerError = error.message;
        this.setStatus(error.message, "error");
      } finally {
        this.loading.register = false;
      }
    },
    handleLogout() {
      this.clearAuthState();
      this.setStatus("你已退出当前账号。");
    },
    async openAdminPanel() {
      this.adminPanelOpen = true;
      if (this.users.length === 0) {
        try {
          await this.loadUsers();
        } catch (error) {
          this.setStatus(error.message, "error");
        }
      }
    },
    closeAdminPanel() {
      this.adminPanelOpen = false;
    },
    promptDelete(todo) {
      this.pendingDeleteTodo = todo;
    },
    cancelDelete() {
      if (this.loading.actionId) {
        return;
      }
      this.pendingDeleteTodo = null;
      this.setStatus("已取消删除。");
    },
    async handleCreateTodo() {
      if (!this.isAuthenticated) {
        this.setStatus("请先登录。", "error");
        return;
      }
      const validationMessage = this.validateTitle(this.todoForm.title);
      this.titleError = validationMessage;
      if (validationMessage) {
        this.setStatus("请先修正输入内容。", "error");
        return;
      }
      this.loading.createTodo = true;
      try {
        await this.apiFetch("/api/todos", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            title: this.todoForm.title,
            priority: this.todoForm.priority
          })
        }, "添加任务失败，请稍后再试。");
        this.todoForm.title = "";
        this.todoForm.priority = "MEDIUM";
        this.titleError = "";
        await this.loadTodos();
        this.setStatus("任务已添加。", "success");
      } catch (error) {
        this.setStatus(error.message, "error");
      } finally {
        this.loading.createTodo = false;
      }
    },
    async refreshTodos() {
      try {
        await this.loadTodos();
        this.setStatus("任务列表已刷新。");
      } catch (error) {
        this.setStatus(error.message, "error");
      }
    },
    async refreshUsers() {
      try {
        await this.loadCurrentUser();
        await this.loadUsers();
        this.setStatus("用户列表已刷新。");
      } catch (error) {
        this.setStatus(error.message, "error");
      }
    },
    startEdit(todo) {
      this.editingId = todo.id;
      this.editingTitle = todo.title;
      this.editingPriority = todo.priority || "MEDIUM";
      this.inlineEditError = "";
      nextTick(() => {
        const input = document.getElementById(`edit-input-${todo.id}`);
        if (input) {
          input.focus();
          input.select();
        }
      });
    },
    cancelEdit() {
      this.editingId = null;
      this.editingTitle = "";
      this.editingPriority = "MEDIUM";
      this.inlineEditError = "";
      this.setStatus("已取消编辑。");
    },
    async saveTodo(id) {
      const validationMessage = this.validateTitle(this.editingTitle);
      this.inlineEditError = validationMessage;
      if (validationMessage) {
        return;
      }
      this.loading.actionId = id;
      try {
        await this.apiFetch(`/api/todos/${id}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            title: this.editingTitle,
            priority: this.editingPriority
          })
        }, "保存失败，请稍后再试。");
        this.editingId = null;
        this.editingTitle = "";
        this.editingPriority = "MEDIUM";
        this.inlineEditError = "";
        await this.loadTodos();
        this.setStatus("任务已更新。", "success");
      } catch (error) {
        this.setStatus(error.message, "error");
      } finally {
        this.loading.actionId = null;
      }
    },
    async markDone(id) {
      this.loading.actionId = id;
      try {
        await this.apiFetch(`/api/todos/${id}/done`, { method: "PUT" }, "操作失败，请稍后再试。");
        await this.loadTodos();
        this.setStatus("任务已标记为完成。", "success");
      } catch (error) {
        this.setStatus(error.message, "error");
      } finally {
        this.loading.actionId = null;
      }
    },
    async confirmDelete() {
      if (!this.pendingDeleteTodo) {
        return;
      }
      const id = this.pendingDeleteTodo.id;
      this.loading.actionId = id;
      try {
        await this.apiFetch(`/api/todos/${id}`, { method: "DELETE" }, "删除失败，请稍后再试。");
        await this.loadTodos();
        this.pendingDeleteTodo = null;
        this.setStatus("任务已删除。", "success");
      } catch (error) {
        this.setStatus(error.message, "error");
      } finally {
        this.loading.actionId = null;
      }
    },
    async toggleUserEnabled(user) {
      this.loading.userActionId = user.id;
      const action = user.enabled ? "disable" : "enable";
      try {
        await this.apiFetch(
          `/api/users/${user.id}/${action}`,
          { method: "PUT" },
          action === "enable" ? "启用用户失败，请稍后再试。" : "禁用用户失败，请稍后再试。"
        );
        await this.loadCurrentUser();
        await this.loadUsers();
        this.setStatus(action === "enable" ? "账号已启用。" : "账号已禁用。", "success");
      } catch (error) {
        this.setStatus(error.message, "error");
      } finally {
        this.loading.userActionId = null;
      }
    }
  },
  mounted() {
    this._onKeydown = (event) => {
      if (event.key === "Escape") {
        if (this.pendingDeleteTodo) {
          this.cancelDelete();
          return;
        }
        if (this.adminPanelOpen) {
          this.closeAdminPanel();
        }
      }
    };
    window.addEventListener("keydown", this._onKeydown);
    if (this.isAuthenticated) {
      this.afterLoginSuccess().catch((error) => {
        this.clearAuthState();
        this.setStatus(error.message, "error");
      });
    }
  },
  beforeUnmount() {
    if (this._onKeydown) {
      window.removeEventListener("keydown", this._onKeydown);
    }
  }
};
</script>

<style>
:global(body) {
  margin: 0;
  min-height: 100vh;
  font-family: "Segoe UI Variable", "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #172033;
  background:
    radial-gradient(circle at top left, rgba(44, 92, 197, 0.16), transparent 28%),
    linear-gradient(180deg, #eef3f9 0%, #e8eef6 100%);
}

:global(*) {
  box-sizing: border-box;
}

:global(:root) {
  --el-color-primary: #2563eb;
  --el-color-primary-light-3: #60a5fa;
  --el-color-primary-light-5: #93c5fd;
  --el-color-primary-light-7: #bfdbfe;
  --el-color-primary-light-9: #eff6ff;
  --el-color-primary-dark-2: #1d4ed8;
  --el-border-radius-base: 8px;
  --el-font-family: "Segoe UI Variable", "PingFang SC", "Microsoft YaHei", sans-serif;
}

:global(button),
:global(input) {
  font: inherit;
}

.app-shell {
  display: grid;
  grid-template-columns: 308px minmax(0, 1fr);
  gap: 18px;
  width: min(1440px, calc(100% - 32px));
  margin: 16px auto;
  min-height: calc(100vh - 32px);
}

.sidebar {
  display: grid;
  align-content: start;
  gap: 16px;
  padding: 22px;
  border-radius: 8px;
  background: #111827;
  color: #f8fafc;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.2);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 6px;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 50px;
  height: 50px;
  border-radius: 8px;
  background: #2563eb;
  font-size: 1.3rem;
  font-weight: 800;
}

.brand-kicker {
  margin: 0 0 4px;
  font-size: 0.74rem;
  letter-spacing: 0.18em;
  color: #94a3b8;
}

.brand h1 {
  margin: 0;
  font-size: 1.72rem;
  line-height: 1.04;
}

.sidebar-panel,
.workspace-hero,
.composer-panel,
.tasks-panel,
.inspector-panel {
  border-radius: 8px;
}

.sidebar-panel {
  padding: 18px;
  background: #1f2937;
  border: 1px solid #334155;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.workspace {
  display: grid;
  gap: 18px;
  align-content: start;
}

.workspace-hero,
.composer-panel,
.tasks-panel,
.inspector-panel {
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(255, 255, 255, 0.76);
  box-shadow: 0 18px 42px rgba(17, 24, 39, 0.08);
  backdrop-filter: blur(12px);
}

.workspace-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(260px, 0.9fr);
  gap: 18px;
  padding: 26px 30px;
}

.hero-kicker {
  margin: 0 0 8px;
  color: #667085;
  font-size: 0.84rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.workspace-hero h2 {
  margin: 0;
  font-size: clamp(2rem, 3vw, 2.85rem);
  line-height: 1.04;
  max-width: 14ch;
}

.hero-summary {
  max-width: 34rem;
  margin: 14px 0 0;
  color: #667085;
  line-height: 1.7;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  align-self: stretch;
}

.hero-stat,
.summary-item {
  padding: 16px 18px;
  border-radius: 8px;
  background: #f8fafc;
}

.hero-stat.accent {
  background: linear-gradient(135deg, #eff6ff 0%, #f0fdfa 100%);
}

.hero-stat span,
.summary-item span {
  display: block;
  font-size: 0.84rem;
  color: #667085;
}

.hero-stat strong,
.summary-item strong {
  display: block;
  margin-top: 10px;
  font-size: 1.8rem;
  line-height: 1;
}

.summary-item strong {
  font-size: 1.02rem;
  line-height: 1.45;
  color: #172033;
}

.workspace-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 18px;
}

.main-column,
.side-column,
.form,
.todo-list,
.todo-editor,
.user-list,
.identity-details {
  display: grid;
  gap: 14px;
}

.side-column {
  align-content: start;
}

.composer-panel,
.tasks-panel,
.inspector-panel {
  padding: 22px;
}

.panel-head,
.todo-topline,
.todo-actions,
.todo-editor-actions,
.identity-tags,
.task-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.panel-head.compact {
  margin-bottom: 4px;
}

.panel-head h2,
.panel-head h3 {
  margin: 0 0 6px;
  font-size: 1.1rem;
}

.panel-head p,
.toolbar-note,
.identity-meta,
.status {
  margin: 0;
  color: #667085;
  line-height: 1.6;
}

.sidebar .panel-head p,
.sidebar .identity-meta {
  color: #cbd5e1;
}

.input,
.inline-input {
  width: 100%;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  min-height: 44px;
  padding: 0 14px;
  background: #fff;
  color: #172033;
  outline: none;
  transition: border-color 160ms ease, box-shadow 160ms ease, transform 160ms ease;
}

.sidebar .input,
.sidebar .inline-input {
  border-color: #475569;
  background: #f8fafc;
}

.input:focus,
.inline-input:focus {
  border-color: rgba(37, 99, 235, 0.5);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.1);
  transform: translateY(-1px);
}

.input-large {
  min-height: 58px;
}

.priority-select {
  min-width: 132px;
}

.composer-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
}

.button {
  border: 0;
  border-radius: 8px;
  padding: 12px 18px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 160ms ease, opacity 160ms ease, background 160ms ease, box-shadow 160ms ease;
}

.button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
}

.button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.button-block {
  width: 100%;
}

.button-primary {
  color: #fff;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
}

.button-secondary {
  background: #eef4ff;
  color: #172033;
}

.sidebar .button-secondary {
  background: #e0f2fe;
  color: #075985;
}

.sidebar .button-secondary:hover:not(:disabled) {
  background: #bae6fd;
}

.admin-entry {
  margin-top: 10px;
}

.account-actions {
  display: grid;
  gap: 10px;
  padding-top: 14px;
  margin-top: 4px;
  border-top: 1px solid #334155;
}

.logout-link {
  justify-content: center;
  border: 1px solid #64748b;
  background: transparent;
  color: #e2e8f0;
}

.logout-link:hover:not(:disabled) {
  border-color: #fca5a5;
  background: #fee2e2;
  color: #991b1b;
  box-shadow: none;
}

.button-danger {
  color: #fff;
  background: #dc2626;
}

.ghost {
  border: 1px solid rgba(15, 23, 42, 0.1);
  background: #fff;
  color: inherit;
}

.danger {
  color: #b42318;
}

.filter-group {
  display: inline-flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-chip {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: #f8fafc;
  color: #667085;
  padding: 10px 14px;
  font-weight: 700;
  cursor: pointer;
}

.filter-chip.active {
  background: #eff6ff;
  border-color: rgba(37, 99, 235, 0.28);
  color: #1d4ed8;
}

.toolbar-note {
  font-size: 0.92rem;
}

.search-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  margin-bottom: 2px;
}

.todo-section {
  display: grid;
}

.todo-section + .todo-section {
  margin-top: 8px;
}

.todo-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 0 4px;
}

.todo-section-head h4 {
  margin: 0;
  font-size: 0.95rem;
  color: #475467;
}

.todo-section-head span {
  min-width: 28px;
  padding: 4px 8px;
  border-radius: 999px;
  background: #f2f4f7;
  color: #667085;
  font-size: 0.8rem;
  text-align: center;
  font-weight: 700;
}

.todo-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  padding: 18px 0;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.todo-item:first-child {
  border-top: 0;
}

.todo-editor {
  margin-top: 10px;
  padding: 14px;
  border: 1px solid rgba(37, 99, 235, 0.18);
  border-radius: 8px;
  background: #f8fbff;
  box-shadow: inset 3px 0 0 #2563eb;
}

.todo-editor .priority-select {
  max-width: 180px;
}

.todo-editor-actions {
  justify-content: flex-start;
  gap: 8px;
  padding-top: 2px;
}

.editor-save-button,
.editor-cancel-button {
  min-width: 82px;
  font-weight: 700;
}

.editor-save-button {
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.18);
}

.todo-actions {
  align-self: start;
  justify-content: flex-end;
}

.todo-actions .button {
  min-width: 58px;
  padding: 9px 12px;
  font-size: 0.9rem;
}

.todo-tag,
.priority-pill,
.role-tag,
.user-state,
.todo-id {
  font-size: 0.78rem;
  font-weight: 700;
}

.todo-tag,
.priority-pill,
.role-tag,
.user-state {
  padding: 6px 10px;
  border-radius: 999px;
}

.todo-tag {
  background: #eef2f7;
  color: #667085;
}

.todo-tag.done {
  background: #ecfdf3;
  color: #027a48;
}

.priority-pill {
  background: #eef2ff;
  color: #4338ca;
}

.priority-pill.high {
  background: #fef3f2;
  color: #b42318;
}

.priority-pill.medium {
  background: #fff7ed;
  color: #c2410c;
}

.priority-pill.low {
  background: #ecfdf3;
  color: #027a48;
}

.role-tag {
  background: rgba(79, 70, 229, 0.12);
  color: #4338ca;
}

.role-tag.admin {
  background: rgba(249, 115, 22, 0.14);
  color: #c2410c;
}

.user-state {
  background: rgba(2, 122, 72, 0.12);
  color: #027a48;
}

.user-state.inactive {
  background: rgba(180, 35, 24, 0.12);
  color: #b42318;
}

.sidebar .role-tag {
  background: #dbeafe;
  color: #1e40af;
}

.sidebar .role-tag.admin {
  background: #ffedd5;
  color: #9a3412;
}

.sidebar .user-state {
  background: #dcfce7;
  color: #166534;
}

.sidebar .user-state.inactive {
  background: #fee2e2;
  color: #991b1b;
}

.sidebar .identity-name {
  color: #f8fafc;
}

.todo-id {
  color: #98a2b3;
}

.todo-title {
  margin: 0;
  font-size: 1.08rem;
  line-height: 1.55;
}

.todo-title.done {
  color: #98a2b3;
  text-decoration: line-through;
}

.identity-name {
  font-size: 1.08rem;
  font-weight: 700;
}

.progress-ring {
  --progress: v-bind(progressPercent);
  display: grid;
  place-items: center;
  width: 170px;
  height: 170px;
  margin: 8px auto 16px;
  border-radius: 50%;
  background:
    radial-gradient(circle at center, #fff 58%, transparent 59%),
    conic-gradient(#4453d7 calc(var(--progress) * 1%), #e4e7ec 0);
}

.progress-number {
  font-size: 2rem;
  font-weight: 800;
}

.progress-label {
  font-size: 0.92rem;
  color: #667085;
}

.summary-list {
  display: grid;
  gap: 12px;
}

.recent-list {
  display: grid;
  gap: 12px;
}

.summary-item {
  min-height: 82px;
}

.recent-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 8px;
  background: #f8fafc;
}

.recent-item strong {
  font-size: 0.98rem;
  line-height: 1.5;
}

.recent-badge,
.recent-meta {
  font-size: 0.82rem;
  color: #667085;
}

.recent-badge {
  color: #027a48;
  font-weight: 700;
}

.user-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  padding: 14px 0;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.user-row:first-child {
  border-top: 0;
}

.admin-overlay {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.44);
  backdrop-filter: blur(10px);
  z-index: 30;
}

.admin-sheet {
  width: min(780px, 100%);
  max-height: min(80vh, 920px);
  overflow: auto;
  padding: 24px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(255, 255, 255, 0.82);
  box-shadow: 0 30px 70px rgba(15, 23, 42, 0.22);
}

.admin-sheet-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.confirm-sheet {
  width: min(460px, 100%);
  padding: 24px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.97);
  border: 1px solid rgba(255, 255, 255, 0.84);
  box-shadow: 0 30px 70px rgba(15, 23, 42, 0.22);
}

.confirm-body {
  display: grid;
  gap: 6px;
  margin: 18px 0 20px;
  padding: 16px 18px;
  border-radius: 8px;
  background: #f8fafc;
}

.confirm-body strong {
  font-size: 1rem;
  line-height: 1.5;
}

.confirm-body span {
  color: #667085;
  font-size: 0.86rem;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.empty-state {
  display: grid;
  gap: 6px;
  padding: 30px 0 12px;
  color: #667085;
  text-align: center;
  line-height: 1.7;
}

.empty-state strong {
  color: #172033;
  font-size: 1rem;
}

.compact-empty {
  padding: 18px 0 8px;
}

.login-empty {
  padding-top: 38px;
}

.status {
  min-height: 22px;
  font-size: 0.94rem;
}

.status.success {
  color: #027a48;
}

.status.error,
.field-error {
  color: #b42318;
}

.field-error {
  min-height: 20px;
  font-size: 0.9rem;
}

.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

@media (max-width: 1220px) {
  .app-shell,
  .workspace-grid,
  .workspace-hero {
    grid-template-columns: 1fr;
  }

  .sidebar {
    order: 2;
  }
}

@media (max-width: 760px) {
  .app-shell {
    width: calc(100% - 20px);
    margin: 10px auto;
  }

  .sidebar,
  .workspace-hero,
  .composer-panel,
  .tasks-panel,
  .inspector-panel {
    border-radius: 8px;
  }

  .hero-stats,
  .composer-form,
  .search-row,
  .todo-item,
  .user-row {
    grid-template-columns: 1fr;
  }

  .admin-overlay {
    padding: 12px;
  }

  .admin-sheet {
    padding: 18px;
    border-radius: 8px;
  }

  .confirm-sheet {
    padding: 18px;
    border-radius: 8px;
  }
}
</style>
