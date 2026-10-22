// Controller page of the Dungeon audience mode, on whatever device each player uses.
//
// The game publishes a public state through the relay; this page streams it and shows only the
// controls of the current game context. Inputs go back as votes for the current window, or as
// survey answers. The page keeps its credentials per session code in localStorage, so a reload
// rejoins as the same participant.
"use strict";

const API = "api/sessions/";
const app = document.getElementById("app");

const DIRECTIONS = [
  { value: "up", label: "▲", name: "oben", area: "up", key: "ArrowUp" },
  { value: "left", label: "◀", name: "links", area: "left", key: "ArrowLeft" },
  { value: "right", label: "▶", name: "rechts", area: "right", key: "ArrowRight" },
  { value: "down", label: "▼", name: "unten", area: "down", key: "ArrowDown" },
];

const session = {
  code: null,
  token: null,
  pid: null,
  state: null,
  stateAt: 0,
  online: false,
  events: null,
  failures: 0,
  replaced: false,
  surveys: {},
};
const votes = new Map();
const drafts = {};
let screenKey = "";

// --- helpers

function el(tag, attributes = {}, ...children) {
  const node = document.createElement(tag);
  for (const [key, value] of Object.entries(attributes)) {
    if (value === undefined || value === null || value === false) continue;
    if (key === "class") node.className = value;
    else if (key.startsWith("on")) node.addEventListener(key.slice(2), value);
    else node.setAttribute(key, value === true ? "" : value);
  }
  for (const child of children.flat()) {
    if (child === null || child === undefined || child === false) continue;
    node.append(child instanceof Node ? child : document.createTextNode(String(child)));
  }
  return node;
}

/** Text of a survey string in German, falling back to English; `code` spans become <code>. */
function localized(text) {
  const value = text ? (text.de ?? text.en ?? "") : "";
  return value.split("`").map((part, i) => (i % 2 === 1 ? el("code", {}, part) : part));
}

function spaced(code) {
  return code && code.length === 6 ? code.slice(0, 3) + " " + code.slice(3) : code || "";
}

function storageKey(code) {
  return "dungeon-audience:" + code;
}

async function post(path, body) {
  const response = await fetch(API + session.code + path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!response.ok) {
    const error = new Error("HTTP " + response.status);
    error.status = response.status;
    throw error;
  }
  return response.json();
}

// --- session

async function join(code, token) {
  session.code = code;
  const joined = await post("/join", token ? { token } : {});
  session.token = joined.token;
  session.pid = joined.pid;
  session.surveys = joined.surveys;
  localStorage.setItem(storageKey(code), JSON.stringify({ token: joined.token, pid: joined.pid }));
  history.replaceState(null, "", "?code=" + code);
  connect();
}

function connect() {
  if (session.events) session.events.close();
  const url = API + session.code + "/events?token=" + encodeURIComponent(session.token);
  const events = new EventSource(url);
  session.events = events;
  events.addEventListener("state", (event) => {
    const message = JSON.parse(event.data);
    session.state = message.state;
    session.stateAt = performance.now() - message.ageMs;
    session.online = true;
    session.failures = 0;
    render();
  });
  // The same device opened the round in another tab; reconnecting here would take the stream back
  // and the two tabs would disconnect each other.
  events.addEventListener("replaced", () => {
    events.close();
    session.online = false;
    session.replaced = true;
    updateBar();
  });
  events.addEventListener("open", () => {
    session.online = true;
    updateBar();
  });
  events.addEventListener("error", async () => {
    session.online = false;
    session.failures++;
    updateBar();
    // EventSource retries network errors on its own, but gives up after an HTTP error, such as a
    // session that is gone or too many open streams. Repeated failures may mean the session is gone.
    const closed = events.readyState === EventSource.CLOSED;
    if (!closed && session.failures !== 3) return;
    try {
      await post("/join", { token: session.token });
    } catch (error) {
      if (error.status === 404 || error.status === 409) {
        events.close();
        forget();
        showJoin("Diese Runde ist vorbei.");
        return;
      }
    }
    if (closed) setTimeout(() => session.events === events && connect(), 3000);
  });
}

function forget() {
  if (session.code) localStorage.removeItem(storageKey(session.code));
  session.code = session.token = session.pid = session.state = null;
}

async function vote(value) {
  const context = session.state.context;
  votes.set(context.window, value);
  render(true);
  try {
    await post("/input", { token: session.token, type: "vote", window: context.window, value });
  } catch {
    votes.delete(context.window);
    render(true);
  }
}

// --- rendering

/** Rebuilds the screen when what it shows changed; otherwise only the timer updates. */
function render(force = false) {
  const key = currentKey();
  if (force || key !== screenKey) {
    screenKey = key;
    app.replaceChildren(...build().filter(Boolean));
    const focus = app.querySelector("[data-autofocus]");
    if (focus) focus.focus();
  }
  updateBar();
  updateResult();
  updateTimer();
}

function status() {
  const players = session.state && session.state.players;
  return players && players[session.pid];
}

function currentKey() {
  if (!session.code || !session.token) return "join";
  const state = session.state;
  if (!state) return "connecting";
  if (state.phase === "closed") return "closed";
  const own = status() || "joining";
  const context = state.context || {};
  if (own === "play" || own === "spectator") {
    return [own, state.phase, state.paused, context.kind, context.window, context.options, context.multiple,
      context.digits, "windowMs" in context, context.title, context.text].join("|");
  }
  return own + "|" + state.phase;
}

function build() {
  const key = screenKey;
  if (key === "join") return joinScreen();
  if (key === "connecting") return [bar(), el("h1", {}, "Verbinde …")];
  if (key === "closed") {
    return [el("h1", {}, "Die Runde ist vorbei"), el("p", { class: "lead" }, "Danke fürs Mitspielen. Ihr könnt die Seite schließen.")];
  }
  const own = status() || "joining";
  switch (own) {
    case "joining":
      return [bar(), el("h1", {}, "Du bist drin"), el("p", { class: "lead" }, "Gleich erscheint die erste Frage.")];
    case "pre":
      return surveyScreen("pre");
    case "post":
      return surveyScreen("post");
    case "done":
      return [bar(), el("h1", {}, "Danke!"), el("p", { class: "lead" }, "Deine Antworten sind angekommen. Ihr könnt die Seite schließen.")];
    case "spectator":
      return spectatorScreen();
    default:
      return playScreen();
  }
}

function bar() {
  return el("div", { class: "bar" },
    el("span", { class: "code", "aria-label": "Code " + session.code }, spaced(session.code)),
    el("span", { "data-online": true }));
}

/** Shows the connection state in place, so a short interruption keeps entered answers. */
function updateBar() {
  const status = app.querySelector("[data-online]");
  if (!status) return;
  status.textContent = session.replaced
    ? "in einem anderen Tab offen"
    : session.online ? "verbunden" : "keine Verbindung";
  status.classList.toggle("offline", !session.online);
}

function showJoin(error) {
  screenKey = "";
  session.code = null;
  app.replaceChildren(...joinScreen(error));
}

/** The code without spaces or other characters, at most six digits; pasting "123 456" works. */
function digitsOnly(text) {
  return text.replace(/\D/g, "").slice(0, 6);
}

function joinScreen(error) {
  const params = new URLSearchParams(location.search);
  const input = el("input", {
    class: "code-input",
    type: "text",
    inputmode: "numeric",
    autocomplete: "off",
    placeholder: "000000",
    "aria-label": "Code vom Spielbildschirm",
    value: digitsOnly(params.get("code") || ""),
    "data-autofocus": true,
  });
  // Errors show inside the code box, so nothing below it moves.
  const message = el("p", { class: "error", role: "alert" });
  const box = el("div", { class: "code-box" }, input, message);
  const submit = el("button", { class: "button primary", type: "submit" }, "Beitreten");
  const showError = (text) => {
    message.textContent = text;
    box.classList.toggle("invalid", Boolean(text));
  };
  const update = () => {
    const digits = digitsOnly(input.value);
    if (input.value !== digits) input.value = digits;
    submit.disabled = digits.length !== 6;
  };
  input.addEventListener("input", () => {
    update();
    showError("");
  });
  update();
  showError(error || "");
  const form = el("form", {
    class: "field join",
    onsubmit: async (event) => {
      event.preventDefault();
      const code = input.value;
      if (code.length !== 6) return;
      submit.disabled = true;
      // Joining the same round again keeps the participant, for example after closing the tab.
      const stored = localStorage.getItem(storageKey(code));
      try {
        await join(code, stored ? JSON.parse(stored).token : null);
      } catch (failure) {
        session.code = null;
        submit.disabled = false;
        showError(failure.status === 404
          ? "Diesen Code gibt es nicht."
          : failure.status === 409
            ? "Runde voll oder schon vorbei."
            : "Server nicht erreichbar.");
      }
    },
  }, el("label", { for: "code" }, "Code vom Spielbildschirm"), box, submit);
  input.id = "code";
  return [
    el("h1", {}, "Dungeon gemeinsam spielen"),
    el("p", { class: "lead" }, "Alle stimmen ab, die Figur macht, was die meisten wählen."),
    el("div", { class: "controls" }, form),
  ];
}

function spectatorScreen() {
  const state = session.state;
  return [
    bar(),
    el("h1", {}, "Du schaust zu"),
    el("p", { class: "lead" }, "Ohne Einwilligung speichern wir nichts von dir. Dafür stimmst du nicht mit ab."),
    state.phase === "playing" && !state.paused ? timerRow() : null,
    resultBlock(),
  ];
}

function playScreen() {
  const state = session.state;
  if (state.phase === "lobby") {
    return [bar(), el("h1", {}, "Gleich geht es los"), el("p", { class: "lead" }, "Schaut auf den Spielbildschirm. Die Spielleitung startet die Runde.")];
  }
  if (state.phase === "countdown") {
    return [bar(), el("h1", {}, "Gleich geht es los"), el("div", { class: "big", "data-countdown": true }, "")];
  }
  if (state.phase === "ended") {
    return [bar(), el("h1", {}, "Geschafft"), el("p", { class: "lead" }, "Schaut auf den Spielbildschirm.")];
  }
  // The host paused the game: the window and its time stand still, and votes cast so far stay.
  if (state.paused) {
    return [bar(), el("h1", {}, "Pausiert"), el("p", { class: "lead" }, "Gleich geht es weiter. Abgegebene Stimmen bleiben erhalten.")];
  }
  const context = state.context || { kind: "wait" };
  const title = {
    move: "Wohin geht es?",
    choice: context.multiple ? "Antworten wählen" : "Antwort wählen",
    text: "Antwort eintippen",
    code: "Code eingeben",
    continue: context.text ? "Text lesen" : "Text auf dem Spielbildschirm lesen",
    wait: "Schaut auf den Spielbildschirm",
  }[context.kind] || "Schaut auf den Spielbildschirm";
  const controls = {
    move: movePad,
    choice: choicePad,
    text: textEntry,
    code: codePad,
    continue: continuePad,
  }[context.kind];
  return [
    bar(),
    el("h1", {}, title),
    context.kind === "wait" ? null : promptBlock(context),
    context.kind === "wait" ? null : timerRow(),
    resultBlock(),
    controls ? el("div", { class: "controls" }, controls(context)) : null,
  ];
}

// Title and question of the dialog on the game screen, so nobody has to read them from afar.
function promptBlock(context) {
  if (!context.title && !context.text) return null;
  return el("section", { class: "prompt" },
    context.title ? el("h2", {}, context.title) : null,
    context.text ? el("p", {}, context.text) : null);
}

function timerRow() {
  const context = session.state.context || {};
  if (!("windowMs" in context)) {
    const text = context.kind === "move" ? "Die erste Stimme startet die Runde." : "";
    return el("p", { class: "note window" }, text);
  }
  const dots = el("div", { class: "dots", "aria-hidden": "true" });
  for (let i = 0; i < 20; i++) dots.append(el("i"));
  return el("div", { class: "window", "data-timer": true },
    dots, el("span", { class: "seconds", "aria-label": "Sekunden übrig" }, ""));
}

function updateTimer() {
  const state = session.state;
  if (!state) return;
  const elapsed = performance.now() - session.stateAt;
  const countdown = app.querySelector("[data-countdown]");
  if (countdown) countdown.textContent = Math.max(1, Math.ceil(((state.countdownMs || 0) - elapsed) / 1000));
  const timer = app.querySelector("[data-timer]");
  if (!timer || !state.context) return;
  const remaining = Math.max(0, (state.context.remainingMs || 0) - elapsed);
  const total = Math.max(1, state.context.windowMs || 1);
  const lit = Math.ceil((remaining / total) * 20);
  timer.querySelectorAll("i").forEach((dot, i) => dot.classList.toggle("on", i < lit));
  timer.querySelector(".seconds").textContent = Math.ceil(remaining / 1000);
}

/** Holder of the last window's result; updated in place so entered answers stay. */
function resultBlock() {
  return el("div", { "data-result": true, role: "status" });
}

function updateResult() {
  const holder = app.querySelector("[data-result]");
  if (!holder) return;
  const result = session.state && session.state.result;
  const key = result ? JSON.stringify(result) : "";
  if (holder.dataset.key === key) return;
  holder.dataset.key = key;
  if (!result) {
    holder.className = "";
    holder.replaceChildren();
    return;
  }
  const tally = (result.tally || []).map((entry) => entry.count + " × " + entry.label).join(", ");
  holder.className = "result";
  holder.replaceChildren(...[el("strong", {}, "Gewählt: " + result.chosen), tally ? el("span", {}, tally) : null].filter(Boolean));
}

function ownVote() {
  return votes.get(session.state.context.window);
}

function movePad() {
  const mine = ownVote();
  const buttons = DIRECTIONS.map((direction) => el("button", {
    class: "button " + direction.area + (mine === direction.value ? " picked" : ""),
    "aria-label": direction.name,
    "aria-pressed": String(mine === direction.value),
    "data-key": direction.key,
    onclick: () => vote(direction.value),
  }, direction.label));
  const act = el("button", {
    class: "button primary act" + (mine === "interact" ? " picked" : ""),
    "aria-pressed": String(mine === "interact"),
    "data-key": "Space",
    onclick: () => vote("interact"),
  }, "Benutzen", el("kbd", {}, "Leertaste"));
  const name = mine ? (DIRECTIONS.find((d) => d.value === mine)?.name ?? "Benutzen") : null;
  return [
    el("div", { class: "pad" }, ...buttons, act),
    el("p", { class: "note" }, name ? "Deine Stimme: " + name + ". Du kannst sie bis zum Ende ändern." : "Wähle eine Richtung oder „Benutzen“."),
    keysHint("Auf der Tastatur: Pfeiltasten und Leertaste."),
  ];
}

function choicePad(context) {
  const count = context.options || 0;
  const labels = context.labels || null;
  const mine = ownVote();
  if (!context.multiple) {
    const grid = el("div", { class: "options" + (labels ? " labels" : "") });
    for (let i = 1; i <= count; i++) {
      grid.append(el("button", {
        class: "button" + (mine !== undefined && mine !== i ? " off" : "") + (mine === i ? " picked" : ""),
        "aria-label": labels ? labels[i - 1] : "Antwort " + i,
        "aria-pressed": String(mine === i),
        "data-key": String(i),
        onclick: () => vote(i),
      }, labels ? labels[i - 1] : i));
    }
    return [
      grid,
      el("p", { class: "note" }, mine ? "Deine Stimme: " + (labels ? labels[mine - 1] : mine) + ". Die Antworten stehen auf dem Spielbildschirm." : "Die Antworten stehen auf dem Spielbildschirm."),
      keysHint("Auf der Tastatur: Zifferntasten 1 bis " + count + "."),
    ];
  }
  const sent = Array.isArray(mine) ? mine : [];
  const picked = new Set(sent);
  const grid = el("div", { class: "options" });
  const send = el("button", { class: "button primary", "data-key": "Enter" }, sent.length ? "Neu abschicken" : "Abschicken", el("kbd", {}, "Enter"));
  const refresh = () => {
    grid.querySelectorAll("button").forEach((button, i) => {
      const on = picked.has(i + 1);
      button.classList.toggle("off", !on);
      button.setAttribute("aria-pressed", String(on));
    });
    send.disabled = picked.size === 0;
  };
  for (let i = 1; i <= count; i++) {
    grid.append(el("button", {
      class: "button",
      "aria-label": "Antwort " + i,
      "data-key": String(i),
      onclick: () => {
        picked.has(i) ? picked.delete(i) : picked.add(i);
        refresh();
      },
    }, i));
  }
  send.addEventListener("click", () => vote([...picked].sort((a, b) => a - b)));
  refresh();
  if (picked.size === 0) grid.querySelectorAll("button").forEach((b) => b.classList.remove("off"));
  return [
    grid,
    send,
    el("p", { class: "note" }, sent.length ? "Abgeschickt: " + sent.join(" + ") + ". Ihr stimmt über die ganze Kombination ab." : "Wähle alle passenden Antworten, dann abschicken."),
    keysHint("Auf der Tastatur: Zifferntasten wählen, Enter schickt ab."),
  ];
}

function textEntry() {
  const mine = ownVote();
  const input = el("input", {
    type: "text",
    autocomplete: "off",
    autocapitalize: "off",
    spellcheck: "false",
    maxlength: "120",
    placeholder: "Eure Antwort",
    "aria-label": "Antwort",
    value: mine || "",
  });
  const send = el("button", { class: "button primary", type: "submit" }, mine ? "Ändern" : "Senden");
  const form = el("form", {
    class: "field",
    onsubmit: (event) => {
      event.preventDefault();
      const value = input.value.trim();
      if (value) vote(value);
    },
  }, input, send);
  return [form, el("p", { class: "note" }, mine ? "Deine Antwort: „" + mine + "“" : "Gleiche Antworten werden zusammengezählt.")];
}

function codePad(context) {
  const length = context.digits;
  let digits = ownVote() || "";
  const display = el("div", { class: "display", "aria-live": "polite" }, "");
  const send = el("button", { class: "button primary word", "data-key": "Enter" }, "OK");
  const show = () => {
    display.textContent = digits.padEnd(length, "_");
    send.disabled = digits.length !== length;
  };
  const keypad = el("div", { class: "keypad" });
  for (const key of ["1", "2", "3", "4", "5", "6", "7", "8", "9"]) {
    keypad.append(el("button", { class: "button", "data-key": key, onclick: () => { if (digits.length < length) digits += key; show(); } }, key));
  }
  keypad.append(el("button", { class: "button word", "aria-label": "Löschen", "data-key": "Backspace", onclick: () => { digits = digits.slice(0, -1); show(); } }, "⌫"));
  keypad.append(el("button", { class: "button", "data-key": "0", onclick: () => { if (digits.length < length) digits += "0"; show(); } }, "0"));
  send.addEventListener("click", () => vote(digits));
  keypad.append(send);
  show();
  const mine = ownVote();
  return [
    display,
    keypad,
    el("p", { class: "note" }, mine ? "Deine Stimme: " + mine : "Führende Nullen zählen mit."),
    keysHint("Auf der Tastatur: Ziffern, Rücktaste und Enter."),
  ];
}

function continuePad() {
  const mine = ownVote();
  return [
    el("button", { class: "button primary" + (mine ? " picked" : ""), "data-key": "Enter", onclick: () => vote("continue"), disabled: Boolean(mine) }, mine ? "Bereit" : "Weiter", mine ? null : el("kbd", {}, "Enter")),
    el("p", { class: "note" }, "Es geht weiter, wenn die Mehrheit bereit ist."),
  ];
}

// Keyboard hint that only devices with a mouse show; touch screens have no keys to press.
function keysHint(text) {
  return el("p", { class: "note keys" }, text);
}

// --- keyboard

// On laptops a key presses the control button that carries it in data-key; typing into a field
// keeps its normal meaning.
document.addEventListener("keydown", (event) => {
  if (event.target.closest("input, textarea, select") || event.altKey || event.ctrlKey || event.metaKey) return;
  const key = event.key === " " ? "Space" : event.key;
  const button = app.querySelector(`.controls [data-key="${CSS.escape(key)}"]`);
  if (!button || button.disabled) return;
  event.preventDefault();
  button.click();
});

// --- surveys

// The game registers its surveys with the session; joining hands them to the device.
function surveyScreen(phase) {
  const holder = el("div", { class: "survey" });
  renderSurvey(holder, phase, session.surveys[phase]);
  return [bar(), holder];
}

function renderSurvey(holder, phase, survey) {
  const draft = (drafts[phase] ||= { page: 0, answers: {} });
  const page = survey.pages[draft.page];
  const last = draft.page === survey.pages.length - 1;
  const fields = page.questions.map((question) => questionField(question, draft.answers));
  const message = el("p", { class: "error", role: "alert" });
  const declining = () => declines(survey, draft.answers);
  const next = el("button", { class: "button primary" }, last || declining() ? "Abschicken" : "Weiter");
  const back = draft.page > 0 ? el("button", { class: "button", onclick: () => { draft.page--; renderSurvey(holder, phase, survey); } }, "Zurück") : null;
  next.addEventListener("click", async () => {
    if (!declining()) {
      const problems = page.questions.map((question, i) => markProblem(fields[i], question, draft.answers[question.id]));
      if (problems.some(Boolean)) {
        fields[problems.findIndex(Boolean)].scrollIntoView({ behavior: "smooth", block: "center" });
        return;
      }
      if (!last) {
        draft.page++;
        renderSurvey(holder, phase, survey);
        window.scrollTo(0, 0);
        return;
      }
    }
    next.disabled = true;
    next.textContent = "Wird gesendet …";
    message.textContent = "";
    try {
      await post("/input", { token: session.token, type: "survey", phase, answers: submission(survey, draft.answers, declining()) });
      setTimeout(() => {
        if (status() === phase) {
          next.disabled = false;
          next.textContent = "Abschicken";
          message.textContent = "Die Antworten wurden nicht angenommen. Bitte prüft sie und schickt sie erneut.";
        }
      }, 6000);
    } catch {
      next.disabled = false;
      next.textContent = "Abschicken";
      message.textContent = "Senden hat nicht geklappt. Prüft die Verbindung und versucht es noch einmal.";
    }
  });
  holder.replaceChildren(...[
    el("h1", {}, localized(survey.title)),
    survey.pages.length > 1 ? el("p", { class: "note" }, "Seite " + (draft.page + 1) + " von " + survey.pages.length) : null,
    page.title ? el("p", { class: "lead" }, localized(page.title)) : null,
    ...fields,
    el("div", { class: "nav" }, back, next, message),
  ].filter(Boolean));
  holder.onchange = () => { next.textContent = last || declining() ? "Abschicken" : "Weiter"; };
}

function declines(survey, answers) {
  return survey.pages.some((page) => page.questions.some((question) => {
    const answer = answers[question.id];
    return (question.options || []).some((option) => option.declinesConsent && answer && answer.selected === option.id);
  }));
}

/** Answers to send: everything answered, or nothing but the declining choice. */
function submission(survey, answers, declining) {
  const result = {};
  for (const page of survey.pages) {
    for (const question of page.questions) {
      const answer = answers[question.id];
      if (answer === undefined || question.type === "info") continue;
      if (declining && !(question.options || []).some((o) => o.declinesConsent && answer.selected === o.id)) continue;
      result[question.id] = answer;
    }
  }
  return result;
}

function problemOf(question, answer) {
  if (question.type === "info") return null;
  const empty = answer === undefined || (question.type === "matrix" && Object.keys(answer).length === 0);
  if (empty) return question.required ? "Bitte beantworten." : null;
  switch (question.type) {
    case "shortText":
    case "longText": {
      const max = question.maxLength || (question.type === "shortText" ? 50 : 10000);
      if (answer.length > max) return "Höchstens " + max + " Zeichen.";
      return null;
    }
    case "number":
      if (!Number.isFinite(answer)) return "Bitte eine Zahl eingeben.";
      if (!question.decimals && !Number.isInteger(answer)) return "Bitte eine ganze Zahl eingeben.";
      if (question.min !== undefined && answer < question.min) return "Mindestens " + question.min + ".";
      if (question.max !== undefined && answer > question.max) return "Höchstens " + question.max + ".";
      return null;
    case "singleChoice":
    case "multiChoice":
    case "dropdown": {
      const selected = [].concat(answer.selected);
      if (selected.length === 0) return question.required ? "Bitte beantworten." : null;
      if (selected.includes("other") && !(answer.otherText || "").trim()) return "Bitte „Sonstiges“ ausfüllen.";
      return null;
    }
    case "matrix":
      if (question.required && Object.keys(answer).length < question.rows.length) return "Bitte jede Zeile beantworten.";
      return null;
    default:
      return null;
  }
}

function markProblem(field, question, answer) {
  const problem = problemOf(question, answer);
  field.classList.toggle("invalid", Boolean(problem));
  field.querySelector(".problem").textContent = problem || "";
  return problem;
}

function questionField(question, answers) {
  const set = (value) => {
    if (value === undefined) delete answers[question.id];
    else answers[question.id] = value;
  };
  const head = el("legend", { class: "title" }, localized(question.text), question.required ? el("span", { class: "required" }, " *") : null);
  const parts = [head];
  if (question.description) parts.push(el("div", { class: "description" }, localized(question.description)));
  const current = answers[question.id];
  switch (question.type) {
    case "info":
      break;
    case "shortText":
    case "longText": {
      const tag = question.type === "longText" ? "textarea" : "input";
      const input = el(tag, {
        type: tag === "input" ? "text" : null,
        rows: tag === "textarea" ? "4" : null,
        maxlength: String(question.maxLength || (question.type === "shortText" ? 50 : 10000)),
        "aria-label": question.text.de ?? question.text.en,
      });
      input.value = current || "";
      input.addEventListener("input", () => set(input.value.trim() ? input.value : undefined));
      parts.push(input);
      break;
    }
    case "number": {
      const input = el("input", { type: "number", inputmode: question.decimals ? "decimal" : "numeric", min: question.min, max: question.max, step: question.decimals ? "any" : "1" });
      input.value = current ?? "";
      input.addEventListener("input", () => set(input.value === "" ? undefined : Number(input.value)));
      parts.push(input);
      break;
    }
    case "scale": {
      const row = el("div", { class: "scale" });
      for (let value = question.min ?? 1; value <= (question.max ?? 5); value++) {
        const button = el("button", { type: "button", class: "button", "aria-pressed": String(current === value) }, value);
        button.addEventListener("click", () => {
          set(answers[question.id] === value ? undefined : value);
          row.querySelectorAll("button").forEach((b) => b.setAttribute("aria-pressed", String(Number(b.textContent) === answers[question.id])));
          row.dispatchEvent(new Event("change", { bubbles: true }));
        });
        row.append(button);
      }
      parts.push(row);
      if (question.minLabel || question.maxLabel) {
        parts.push(el("div", { class: "scale-labels" }, el("span", {}, localized(question.minLabel)), el("span", {}, localized(question.maxLabel))));
      }
      break;
    }
    case "singleChoice":
    case "multiChoice":
      parts.push(...choiceInputs(question, current, set));
      break;
    case "dropdown": {
      const select = el("select", { "aria-label": question.text.de ?? question.text.en },
        el("option", { value: "" }, "Bitte wählen"),
        ...question.options.map((option) => el("option", { value: option.id }, (option.text.de ?? option.text.en).replaceAll("`", ""))));
      select.value = current ? current.selected : "";
      select.addEventListener("change", () => set(select.value ? { selected: select.value } : undefined));
      parts.push(select);
      break;
    }
    case "matrix": {
      const value = { ...(current || {}) };
      for (const rowDef of question.rows) {
        const row = el("div", { class: "scale" });
        for (const column of question.columns) {
          const button = el("button", { type: "button", class: "button", "aria-pressed": String(value[rowDef.id] === column.id) }, localized(column.text));
          button.addEventListener("click", () => {
            value[rowDef.id] = column.id;
            set({ ...value });
            row.querySelectorAll("button").forEach((b, i) => b.setAttribute("aria-pressed", String(question.columns[i].id === value[rowDef.id])));
            row.dispatchEvent(new Event("change", { bubbles: true }));
          });
          row.append(button);
        }
        parts.push(el("div", { class: "matrix-row" }, el("span", {}, localized(rowDef.text)), row));
      }
      break;
    }
  }
  parts.push(el("div", { class: "problem", role: "alert" }));
  return el("fieldset", { class: "question" }, ...parts);
}

function choiceInputs(question, current, set) {
  const multi = question.type === "multiChoice";
  const name = "q-" + question.id;
  const options = [...question.options];
  if (question.other) options.push({ id: "other", text: { de: "Sonstiges" } });
  let selected = current ? [].concat(current.selected) : [];
  let otherText = current && current.otherText ? current.otherText : "";
  const other = el("input", { type: "text", placeholder: "Sonstiges", maxlength: "200", "aria-label": "Sonstiges" });
  other.value = otherText;
  const update = () => {
    if (selected.length === 0) return set(undefined);
    const answer = { selected: multi ? selected : selected[0] };
    if (selected.includes("other")) answer.otherText = otherText;
    set(answer);
  };
  const inputs = options.map((option) => {
    const input = el("input", { type: multi ? "checkbox" : "radio", name, value: option.id });
    input.checked = selected.includes(option.id);
    input.addEventListener("change", () => {
      if (multi) selected = input.checked ? [...selected, option.id] : selected.filter((id) => id !== option.id);
      else selected = [option.id];
      update();
    });
    return el("label", { class: "choice" }, input, el("span", {}, localized(option.text)));
  });
  other.addEventListener("input", () => {
    otherText = other.value;
    if (other.value && !selected.includes("other")) {
      selected = multi ? [...selected, "other"] : ["other"];
      inputs.forEach((label) => {
        const box = label.querySelector("input");
        box.checked = selected.includes(box.value);
      });
    }
    update();
  });
  return question.other ? [...inputs, other] : inputs;
}

// --- start

function start() {
  render();
  // A link with a code, such as the QR code, joins right away; the join form shows any error. A
  // stored token for the code rejoins as the same participant.
  const code = digitsOnly(new URLSearchParams(location.search).get("code") || "");
  if (code.length === 6) app.querySelector("form.join").requestSubmit();
  setInterval(updateTimer, 200);
}

start();
