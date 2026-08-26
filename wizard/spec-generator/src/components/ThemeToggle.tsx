import { MonitorIcon, MoonIcon, SunIcon } from "lucide-react";
import { useTheme } from "next-themes";
import { Button } from "./ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";

const preferences = {
  system: { label: "System", icon: MonitorIcon },
  light: { label: "Hell", icon: SunIcon },
  dark: { label: "Dunkel", icon: MoonIcon },
} as const;

type ThemePreference = keyof typeof preferences;

function isThemePreference(value: string | undefined): value is ThemePreference {
  return value === "system" || value === "light" || value === "dark";
}

export function ThemeToggle({ className }: { className?: string }) {
  const { resolvedTheme, setTheme, theme } = useTheme();
  const preference = isThemePreference(theme) ? theme : "system";
  const ActiveIcon = resolvedTheme === "dark" ? MoonIcon : SunIcon;
  const title = `Darstellung: ${preferences[preference].label}`;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        render={
          <Button
            type="button"
            variant="outline"
            size="icon-sm"
            className={className}
            aria-label={title}
            title={title}
          />
        }
      >
        <ActiveIcon className="size-4" />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-36">
        <DropdownMenuRadioGroup
          value={preference}
          onValueChange={(value) => {
            if (isThemePreference(value)) setTheme(value);
          }}
        >
          {Object.entries(preferences).map(([value, option]) => {
            const Icon = option.icon;
            return (
              <DropdownMenuRadioItem key={value} value={value}>
                <Icon className="size-4" />
                {option.label}
              </DropdownMenuRadioItem>
            );
          })}
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
