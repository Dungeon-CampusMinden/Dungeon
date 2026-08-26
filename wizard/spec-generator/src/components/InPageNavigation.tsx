import { TABS, type TabId } from "@/data/Tabs";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";
import { ArrowLeftIcon, ArrowRightIcon } from "lucide-react";

export function InPageNavigation({ tab, setTab, disabled = false }: {
  tab: TabId;
  setTab: (tab: TabId) => void;
  disabled?: boolean;
}) {
  const currentIndex = TABS.findIndex((entry) => entry.value === tab);

  if (currentIndex === -1 || currentIndex === TABS.length - 1) return null;

  const currentStep = currentIndex + 1;
  const isFirstTab = currentIndex === 0;

  const changeTab = (offset: number) => {
    const nextTab = TABS[currentIndex + offset];
    if (nextTab) setTab(nextTab.value);
  };

  return (
    <nav aria-label="Seitennavigation" className="mt-8 border-t border-border pt-6">
      <div className="grid grid-cols-2 gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={() => changeTab(-1)}
          disabled={disabled || isFirstTab}
          className="gap-2"
        >
          <ArrowLeftIcon className="size-4" />
          Zurück
        </Button>
        <Button
          type="button"
          onClick={() => changeTab(1)}
          disabled={disabled}
          className="gap-2"
        >
          Weiter
          <ArrowRightIcon className="size-4" />
        </Button>
      </div>
      <div className="mt-4 flex flex-col gap-1.5">
        <div className="flex items-center justify-between text-xs text-muted-foreground">
          <span>Fortschritt</span>
          <span>Schritt {currentStep} von {TABS.length}</span>
        </div>
        <Progress
          value={(currentStep / TABS.length) * 100}
          aria-label="Fortschritt"
          getAriaValueText={() => `Schritt ${currentStep} von ${TABS.length}`}
          className="w-full"
        />
      </div>
    </nav>
  );
}
