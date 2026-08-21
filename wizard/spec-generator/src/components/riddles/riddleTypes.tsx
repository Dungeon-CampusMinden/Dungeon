import type {
  HintSeverity,
  Riddle,
  RiddleDifficulty,
} from "@/data/DeerSchema";
import { Util } from "@/data/Util";
import { CircleQuestionMarkIcon, KeyboardIcon, PackageSearchIcon } from "lucide-react";

export const INPUT_TYPES = [
  {
    value: "collection",
    label: "Sammeln",
    description: "Die Spieler finden alle Materialien einer Informationsquelle",
    icon: PackageSearchIcon,
  },
  {
    value: "numeric",
    label: "Zahleneingabe",
    description: "Die Spieler geben einen Zahlencode ein",
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

/** Ordered from the most subtle to the most explicit hint. */
export const HINT_SEVERITIES: { value: HintSeverity; label: string }[] = [
  { value: "orientation", label: "Orientierung" },
  { value: "approach", label: "Ansatz" },
  { value: "solution", label: "Lösung" },
];

export function getInputType(type: string) {
  return INPUT_TYPES.find((inputType) => inputType.value === type);
}

export function getRiddleDifficulty(difficulty: string) {
  return RIDDLE_DIFFICULTIES.find((item) => item.value === difficulty);
}

export function getHintSeverity(severity: string) {
  return HINT_SEVERITIES.find((item) => item.value === severity);
}

/** Position of a hint severity in the escalation order, used for sorting. */
export function getHintSeverityOrder(severity: string) {
  const index = HINT_SEVERITIES.findIndex((item) => item.value === severity);
  return index < 0 ? HINT_SEVERITIES.length : index;
}

export function InputTypeIcon({ type, size = 16 }: { type: string; size?: number }) {
  const inputType = getInputType(type);
  if (!inputType) {
    return <CircleQuestionMarkIcon size={size} />;
  }
  const Icon = inputType.icon;
  return <Icon size={size} />;
}

export function createRiddle(): Riddle {
  return {
    id: Util.generateUniqueId("r"),
    title: "Neues Rätsel",
    difficulty: "easy",
    estimatedMinutes: 5,
    learningObjectiveIds: [],
    informationSources: [],
    inputs: [],
    hints: [],
  };
}
