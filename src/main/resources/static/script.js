const apiBase = "/api";
let currentGroupId = null;

// Load all groups
async function loadGroups() {
  const res = await fetch(`${apiBase}/groups`);
  const groups = await res.json();

  const groupList = document.getElementById("group-list");
  groupList.innerHTML = "";

  groups.forEach((g) => {
    const li = document.createElement("li");
    li.textContent = `${g.name} (${g.description})`;
    li.onclick = () => selectGroup(g.id, g.name);
    groupList.appendChild(li);
  });
}

async function createGroup() {
  const name = document.getElementById("group-name").value.trim();
  const desc = document.getElementById("group-desc").value.trim();
  if (!name) return alert("Enter group name");

  await fetch(`${apiBase}/groups?name=${name}&description=${desc}`, {
    method: "POST",
  });
  document.getElementById("group-name").value = "";
  document.getElementById("group-desc").value = "";
  loadGroups();
}

async function selectGroup(id, name) {
  currentGroupId = id;
  document.getElementById("current-group-name").textContent = name;
  loadMembers();
  loadExpenses();
  loadBalance();
}

// ---------------- MEMBERS ----------------
async function loadMembers() {
  const res = await fetch(`${apiBase}/groups/${currentGroupId}/members`);
  const members = await res.json();

  const list = document.getElementById("member-list");
  const payerSelect = document.getElementById("payer-select");
  list.innerHTML = "";
  payerSelect.innerHTML = "";

  members.forEach((m) => {
    const li = document.createElement("li");
    li.innerHTML = `<span>${m.name}</span>
                    <button onclick="deleteMember(${m.id})">Delete</button>`;
    list.appendChild(li);

    const opt = document.createElement("option");
    opt.value = m.id;
    opt.textContent = m.name;
    payerSelect.appendChild(opt);
  });
}

async function addMember() {
  if (!currentGroupId) return alert("Select a group first");
  const name = document.getElementById("member-name").value.trim();
  if (!name) return alert("Enter friend name");

  await fetch(`${apiBase}/groups/${currentGroupId}/members?name=${name}`, {
    method: "POST",
  });
  document.getElementById("member-name").value = "";
  loadMembers();
}

async function deleteMember(id) {
  await fetch(`${apiBase}/groups/${currentGroupId}/members/${id}`, {
    method: "DELETE",
  });
  loadMembers();
}

// ---------------- EXPENSES ----------------
async function addExpense() {
  if (!currentGroupId) return alert("Select a group first");
  const desc = document.getElementById("expense-desc").value.trim();
  const amount = document.getElementById("expense-amount").value;
  const payerId = document.getElementById("payer-select").value;

  if (!desc || !amount || !payerId) return alert("Fill all fields");

  await fetch(
    `${apiBase}/groups/${currentGroupId}/expenses?payerId=${payerId}&amount=${amount}&description=${desc}`,
    { method: "POST" }
  );

  document.getElementById("expense-desc").value = "";
  document.getElementById("expense-amount").value = "";
  loadExpenses();
  loadBalance();
}

async function loadExpenses() {
  const res = await fetch(`${apiBase}/groups/${currentGroupId}/expenses`);
  const expenses = await res.json();
  const list = document.getElementById("expense-list");
  list.innerHTML = "";

  expenses.forEach((e) => {
    const li = document.createElement("li");
    li.innerHTML = `<span>${e.description}: ₹${e.amount} (Paid by: ${e.payer.name})</span>
                    <div>
                      <button onclick="editExpense(${e.id})">Edit</button>
                      <button onclick="deleteExpense(${e.id})">Delete</button>
                    </div>`;
    list.appendChild(li);
  });
}

async function deleteExpense(id) {
  await fetch(`${apiBase}/groups/${currentGroupId}/expenses/${id}`, {
    method: "DELETE",
  });
  loadExpenses();
  loadBalance();
}

// ---------------- BALANCE ----------------
async function loadBalance() {
  const res = await fetch(`${apiBase}/groups/${currentGroupId}/settlements`);
  const settlements = await res.json();

  const list = document.getElementById("balance-list");
  list.innerHTML = "";
  document.getElementById("settlement-list").innerHTML = "";

  if (settlements.balances) {
    let total = settlements.total || 0;
    document.getElementById("total-bill").textContent = `₹${total}`;

    for (const [name, balance] of Object.entries(settlements.balances)) {
      const li = document.createElement("li");
      li.textContent = `${name}: ₹${balance}`;
      list.appendChild(li);
    }
  }
}

async function showSettlements() {
  const res = await fetch(`${apiBase}/groups/${currentGroupId}/who-owes`);
  const data = await res.json();
  const list = document.getElementById("settlement-list");
  list.innerHTML = "";

  data.forEach((s) => {
    const li = document.createElement("li");
    li.textContent = s;
    list.appendChild(li);
  });
}

// Initial load
loadGroups();
