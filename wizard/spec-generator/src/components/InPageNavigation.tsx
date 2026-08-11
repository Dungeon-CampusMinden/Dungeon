import { TABS } from "@/data/Tabs";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";

export function InPageNavigation({ tab, setTab }: { tab: string; setTab: (tab: string) => void }) {
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
      <Progress
        value={(currentStep / TABS.length) * 100}
        aria-label="Fortschritt"
        getAriaValueText={() => `Schritt ${currentStep} von ${TABS.length}`}
        className="w-full gap-2"
      >
        <div className="flex w-full justify-between text-sm">
          <span>Fortschritt</span>
          <span className="text-muted-foreground">
            Schritt {currentStep} von {TABS.length}
          </span>
        </div>
      </Progress>
      <div className="mt-4 grid grid-cols-2 gap-2">
        <Button type="button" variant="outline" onClick={() => changeTab(-1)} disabled={isFirstTab}>
          Zurück
        </Button>
        <Button type="button" onClick={() => changeTab(1)} disabled={isLastTab}>
          Weiter
        </Button>
      </div>
    </nav>
  );
}
