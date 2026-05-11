(function () {
  "use strict";

  const state = {
    passengerId: null,
    driverId: null,
    tripId: null,
    tripStatus: null,
    token: null,
  };

  const $ = (id) => document.getElementById(id);

  function bases() {
    const u = $("baseUser").value.replace(/\/$/, "");
    const t = $("baseTrip").value.replace(/\/$/, "");
    const n = $("baseNotif").value.replace(/\/$/, "");
    return { user: u + "/api", trip: t + "/api", notif: n + "/api" };
  }

  function logLine(msg, data) {
    const el = $("log");
    const time = new Date().toISOString().slice(11, 23);
    let line = `[${time}] ${msg}`;
    if (data !== undefined) {
      line += "\n" + (typeof data === "string" ? data : JSON.stringify(data, null, 2));
    }
    el.textContent = line + "\n\n" + el.textContent;
  }

  async function request(method, url, body) {
    const opts = { method, headers: {} };
    if (body !== undefined) {
      opts.headers["Content-Type"] = "application/json";
      opts.body = JSON.stringify(body);
    }
    logLine(`${method} ${url}`, body);
    const res = await fetch(url, opts);
    const text = await res.text();
    let parsed = text;
    try {
      parsed = text ? JSON.parse(text) : null;
    } catch {
      parsed = text;
    }
    if (!res.ok) {
      logLine(`Ошибка ${res.status}`, parsed);
      throw new Error((parsed && parsed.message) || res.statusText || String(res.status));
    }
    logLine(`Ответ ${res.status}`, parsed);
    return parsed;
  }

  function uid(prefix) {
    return `${prefix}_${Date.now()}`;
  }

  function renderState() {
    $("stPassenger").textContent = state.passengerId ?? "—";
    $("stDriver").textContent = state.driverId ?? "—";
    $("stTrip").textContent = state.tripId ?? "—";
    $("stStatus").textContent = state.tripStatus ?? "—";
    $("stToken").textContent = state.token ? state.token.slice(0, 48) + "…" : "—";
  }

  async function refreshTrip() {
    const { trip } = bases();
    if (!state.tripId) return;
    const data = await request("GET", `${trip}/trips/${state.tripId}`);
    state.tripStatus = data.status ?? null;
    if (data.driverId != null) state.driverId = data.driverId;
    renderState();
    return data;
  }

  const actions = {
    async login() {
      const { user } = bases();
      const email = state.passengerId ? `p${state.passengerId}@demo.local` : `guest_${Date.now()}@demo.local`;
      const data = await request("POST", `${user}/auth/login`, { email, password: "demo" });
      state.token = data.token ?? null;
      renderState();
    },

    async registerPassenger() {
      const { user } = bases();
      const id = uid("pass");
      const data = await request("POST", `${user}/passengers`, {
        name: "Пассажир " + id,
        email: `${id}@demo.local`,
        phone: "+79000000000",
        password: "demo",
      });
      state.passengerId = data.id;
      renderState();
    },

    async registerDriver() {
      const { user } = bases();
      const id = uid("drv");
      const data = await request("POST", `${user}/drivers`, {
        name: "Водитель " + id,
        email: `${id}@demo.local`,
        phone: "+79000000001",
        licenseNumber: "LIC-" + id,
        password: "demo",
      });
      state.driverId = data.id;
      renderState();
    },

    async listDrivers() {
      const { user } = bases();
      await request("GET", `${user}/drivers/available/list`);
    },

    async createTrip() {
      const { trip } = bases();
      if (!state.passengerId) {
        logLine("Сначала зарегистрируйте пассажира (шаг 2).");
        return;
      }
      const data = await request("POST", `${trip}/trips`, {
        passengerId: state.passengerId,
        origin: "ул. Ленина, 1",
        destination: "Вокзал",
      });
      state.tripId = data.id;
      state.tripStatus = data.status;
      if (data.driverId != null) state.driverId = data.driverId;
      renderState();
    },

    async statusStarted() {
      const { trip } = bases();
      if (!state.tripId) {
        logLine("Нет активной поездки.");
        return;
      }
      const data = await request("PATCH", `${trip}/trips/${state.tripId}/status`, { status: "STARTED" });
      state.tripStatus = data.status;
      renderState();
    },

    async statusCompleted() {
      const { trip } = bases();
      if (!state.tripId) {
        logLine("Нет активной поездки.");
        return;
      }
      const data = await request("PATCH", `${trip}/trips/${state.tripId}/status`, { status: "COMPLETED" });
      state.tripStatus = data.status;
      renderState();
    },

    async statusCancelled() {
      const { trip } = bases();
      if (!state.tripId) {
        logLine("Нет активной поездки.");
        return;
      }
      const data = await request("PATCH", `${trip}/trips/${state.tripId}/status`, { status: "CANCELLED" });
      state.tripStatus = data.status;
      renderState();
    },

    async rateTrip() {
      const { trip } = bases();
      if (!state.tripId) {
        logLine("Нет активной поездки.");
        return;
      }
      await request("POST", `${trip}/trips/${state.tripId}/rate`, { rating: 5 });
    },

    async loadNotifications() {
      const { notif } = bases();
      if (!state.tripId) {
        logLine("Сначала создайте поездку.");
        return;
      }
      await request("GET", `${notif}/notifications?trip_id=${state.tripId}`);
    },

    async dailyStats() {
      const { trip } = bases();
      await request("GET", `${trip}/stats/daily`);
    },

    async getPassenger() {
      const { user } = bases();
      if (!state.passengerId) {
        logLine("Нет ID пассажира.");
        return;
      }
      await request("GET", `${user}/passengers/${state.passengerId}`);
    },

    async getDriver() {
      const { user } = bases();
      if (!state.driverId) {
        logLine("Нет ID водителя.");
        return;
      }
      await request("GET", `${user}/drivers/${state.driverId}`);
    },

    async getTrip() {
      await refreshTrip();
    },
  };

  document.querySelectorAll("[data-action]").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const name = btn.getAttribute("data-action");
      btn.disabled = true;
      try {
        await actions[name]();
      } catch (e) {
        logLine("Исключение", String(e.message || e));
      } finally {
        btn.disabled = false;
      }
    });
  });

  $("btnRefreshTrip").addEventListener("click", async () => {
    try {
      await refreshTrip();
    } catch (e) {
      logLine("Исключение", String(e.message || e));
    }
  });

  $("btnClearLog").addEventListener("click", () => {
    $("log").textContent = "";
  });

  $("btnFullDemo").addEventListener("click", async () => {
    const btn = $("btnFullDemo");
    btn.disabled = true;
    const order = [
      "login",
      "registerPassenger",
      "registerDriver",
      "listDrivers",
      "createTrip",
      "statusStarted",
      "statusCompleted",
      "rateTrip",
      "loadNotifications",
      "dailyStats",
    ];
    try {
      for (const name of order) {
        await actions[name]();
        await new Promise((r) => setTimeout(r, 200));
      }
    } catch (e) {
      logLine("Полный сценарий прерван", String(e.message || e));
    } finally {
      btn.disabled = false;
    }
  });

  renderState();
})();
