import type { AnyRiddle, RiddleDifficulty } from "@/data/DeerSchema";
import { Util } from "@/data/Util";
import { CircleQuestionMarkIcon, KeyboardIcon, PackageSearchIcon } from "lucide-react";

export const RIDDLE_TYPES = [
  {
    value: "collection",
    label: "Sammeln",
    description: "Die Spieler finden Hinweise an einem Ort oder in einem Behälter",
    icon: PackageSearchIcon,
  },
  {
    value: "input",
    label: "Eingabe",
    description: "Die Spieler geben eine Lösung an einem Gerät ein",
    icon: KeyboardIcon,
  },
] as const;

export const RIDDLE_DIFFICULTIES: { value: RiddleDifficulty; label: string; className: string }[] = [
  { value: "easy", label: "Leicht", className: "bg-green-600 text-white" },
  { value: "medium", label: "Mittel", className: "bg-yellow-600 text-white" },
  { value: "hard", label: "Schwer", className: "bg-red-600 text-white" },
];

export const RESOURCE_KINDS = [
  { value: "inline_text", label: "Text" },
  { value: "asset", label: "Datei" },
] as const;

export const RESOURCE_AVAILABILITIES = [
  { value: "visible_in_level", label: "Sichtbar im Raum" },
  { value: "inside_container", label: "In einem Behälter" },
] as const;

export const RESOURCE_PURPOSES = [
  { value: "clue", label: "Hinweis" },
  { value: "context", label: "Kontext" },
  { value: "instruction", label: "Anleitung" },
  { value: "decoy", label: "Ablenkung" },
] as const;

export const COLLECTION_SOURCE_KINDS = [
  { value: "container", label: "Behälter" },
  { value: "world_object", label: "Objekt im Raum" },
] as const;

export function getRiddleType(type: string) {
  return RIDDLE_TYPES.find((riddleType) => riddleType.value === type);
}

export function getRiddleDifficulty(difficulty: string) {
  return RIDDLE_DIFFICULTIES.find((item) => item.value === difficulty);
}

export function RiddleTypeIcon({ type, size = 16 }: { type: string; size?: number }) {
  const riddleType = getRiddleType(type);
  if (!riddleType) {
    return <CircleQuestionMarkIcon size={size} />;
  }
  const Icon = riddleType.icon;
  return <Icon size={size} />;
}

export function createRiddle(type: AnyRiddle["type"]): AnyRiddle {
  const base = {
    id: Util.generateUniqueId("r"),
    title: "Neues Rätsel",
    difficulty: "easy" as RiddleDifficulty,
    estimatedMinutes: 5,
    learningObjectiveIds: [],
    playerFacingTask: "",
    hints: [],
  };

  if (type === "input") {
    return {
      ...base,
      type: "input",
      resources: [],
      parameters: {
        surfaceId: "",
        inputMode: "numeric",
        answer: "",
        showDigitCount: true,
      },
    };
  }

  return {
    ...base,
    type: "collection",
    resources: [],
    parameters: {
      surfaceId: "",
      sourceKind: "container",
      rewardMode: "find_resource",
      resourceIds: [],
    },
  };
}

/** Converts a riddle to another type while keeping all generic properties intact. */
export function convertRiddleType(riddle: AnyRiddle, type: AnyRiddle["type"]): AnyRiddle {
  if (riddle.type === type) return riddle;
  const converted = createRiddle(type);
  return {
    ...converted,
    id: riddle.id,
    title: riddle.title,
    difficulty: riddle.difficulty,
    estimatedMinutes: riddle.estimatedMinutes,
    learningObjectiveIds: [...riddle.learningObjectiveIds],
    playerFacingTask: riddle.playerFacingTask,
    hints: riddle.hints,
    // Input riddles must not carry any resources.
    resources: type === "input" ? [] : riddle.resources,
  } as AnyRiddle;
}
