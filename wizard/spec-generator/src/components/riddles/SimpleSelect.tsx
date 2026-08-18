import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "../ui/select";

export function SimpleSelect({
  options,
  value,
  onChange,
  placeholder,
  className,
  accessibleLabel,
}: {
  options: readonly { value: string; label: string }[];
  value: string;
  onChange: (newValue: string) => void;
  placeholder?: string;
  className?: string;
  accessibleLabel: string;
}) {
  return (
    <Select
      items={options.map((option) => ({ label: option.label, value: option.value }))}
      value={value}
      onValueChange={(newValue) => onChange((newValue as string) ?? "")}
    >
      <SelectTrigger aria-label={accessibleLabel} className={className ?? "w-full"}>
        <SelectValue placeholder={placeholder ?? "Bitte wählen"} />
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
  );
}
