import { TABS, type TabId } from "@/data/Tabs";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";

export function InPageNavigation({ tab, setTab, disabled = false }: {
  tab: TabId;
  setTab: (tab: TabId) => void;
  disabled?: boolean;
}) {
  const currentIndex = TABS.findIndex((entry) => entry.value === tab);

  if (currentIndex === -1) return null;

  const currentStep = currentIndex + 1;
  const isFirstTab = currentIndex === 0;
  const isLastTab = currentIndex === TABS.length - 1;

  const changeTab = (offset: number) => {
    const nextTab = TABS[currentIndex + offset];
    if (nextTab) setTab(nextTab.value);
  };

  return (
    <nav aria-label="Seitennavigation" className="mt-8 border-t border-border pt-6">
      <div className="grid grid-cols-2 gap-2">
        <Button type="button" variant="outline" onClick={() => changeTab(-1)} disabled={disabled || isFirstTab}>
          Zurück
        </Button>
        <Button type="button" onClick={() => changeTab(1)} disabled={disabled || isLastTab}>
          Weiter
        </Button>
      </div>
      <Progress
        value={(currentStep / TABS.length) * 100}
        aria-label="Fortschritt"
        getAriaValueText={() => `Schritt ${currentStep} von ${TABS.length}`}
        className="mt-4 w-full"
      />
    </nav>
  );
}
