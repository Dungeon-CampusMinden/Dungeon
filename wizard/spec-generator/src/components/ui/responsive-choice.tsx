import type { KeyboardEvent } from "react";

import { Button } from "./button";
import { ButtonGroup } from "./button-group";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "./select";

type ChoiceOption<T extends string> = {
  readonly value: T;
  readonly label: string;
};

export function ResponsiveChoice<T extends string>({
  options,
  value,
  onChange,
  accessibleLabel,
}: {
  options: readonly ChoiceOption<T>[];
  value: T;
  onChange: (newValue: T) => void;
  accessibleLabel: string;
}) {
  const selectOption = (event: KeyboardEvent<HTMLButtonElement>, optionIndex: number) => {
    const direction =
      event.key === "ArrowRight" || event.key === "ArrowDown"
        ? 1
        : event.key === "ArrowLeft" || event.key === "ArrowUp"
          ? -1
          : 0;
    const nextIndex =
      event.key === "Home"
        ? 0
        : event.key === "End"
          ? options.length - 1
          : direction === 0
            ? -1
            : (optionIndex + direction + options.length) % options.length;

    if (nextIndex < 0) return;
    event.preventDefault();
    const nextOption = options[nextIndex];
    if (!nextOption) return;
    onChange(nextOption.value);
    event.currentTarget.parentElement
      ?.querySelectorAll<HTMLButtonElement>("[role=radio]")
      [nextIndex]?.focus();
  };

  return (
    <div className="responsive-choice">
      <div className="responsive-choice-select">
        <Select
          items={options.map((option) => ({ label: option.label, value: option.value }))}
          value={value}
          onValueChange={(newValue) => {
            if (newValue !== null) onChange(newValue as T);
          }}
        >
          <SelectTrigger aria-label={accessibleLabel} className="w-full">
            <SelectValue placeholder="Bitte wählen" />
          </SelectTrigger>
          <SelectContent alignItemWithTrigger={false} className="min-w-[max-content]">
            <SelectGroup>
              {options.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
      </div>
      <ButtonGroup
        aria-label={accessibleLabel}
        className="responsive-choice-buttons w-full"
        role="radiogroup"
      >
        {options.map((option, index) => {
          const selected = option.value === value;
          return (
            <Button
              key={option.value}
              aria-checked={selected}
              className="min-w-0 flex-1 px-1.5 text-xs"
              onClick={() => onChange(option.value)}
              onKeyDown={(event) => selectOption(event, index)}
              role="radio"
              tabIndex={selected ? 0 : -1}
              type="button"
              variant={selected ? "default" : "outline"}
            >
              {option.label}
            </Button>
          );
        })}
      </ButtonGroup>
    </div>
  );
}
