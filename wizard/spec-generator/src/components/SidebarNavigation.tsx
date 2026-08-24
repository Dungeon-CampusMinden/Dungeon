import { Tabs, TabsList, TabsTrigger } from "./ui/tabs";
import { CircleAlertIcon, CircleCheckIcon, CircleIcon, CircleXIcon, InfoIcon } from "lucide-react";
import { ErrorChecker, type IssueReport, type IssueSeverity } from "@/data/ErrorChecker";
import { isTabTouched, type TouchedTabs } from "@/data/TabTouchState";
import { isTabId, TABS, type TabId } from "@/data/Tabs";

/** Review has no authored or locally validated input fields, so it has no tab status of its own. */
const UNVALIDATED_TABS = ["review"];

export function SidebarNavigation({
  issueReport,
  touchedTabs,
  tab,
  setTab,
  disabled = false,
  className,
}: {
  issueReport: IssueReport;
  touchedTabs: TouchedTabs;
  tab: TabId;
  setTab: (tab: TabId) => void;
  disabled?: boolean;
  className?: string;
}) {
  return (
    <div className={`panel ${className ?? ""} flex flex-col gap-2`}>
      <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground px-1 mb-1">
        Übersicht
      </h2>
      <Tabs
        value={tab}
        onValueChange={(value) => {
          if (isTabId(value)) setTab(value);
        }}
        orientation="vertical"
        className="mt-0 w-full"
      >
        <TabsList className="bg-transparent w-full flex-col gap-1 p-0">
          {TABS.map((entry) => (
            <TabsTrigger
              key={entry.value}
              value={entry.value}
              disabled={disabled}
              className="w-full justify-start gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition-colors text-muted-foreground hover:bg-muted/70 hover:text-foreground data-active:bg-primary/15 data-active:text-primary data-active:font-semibold"
            >
              <TabStatusIcon severity={getTabSeverity(entry.value, issueReport, touchedTabs)} />
              <span className="truncate">{entry.label}</span>
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>
    </div>
  );
}

/** Feedback is only shown once the user has actually worked on a tab. */
function getTabSeverity(
  tabId: TabId,
  issueReport: IssueReport,
  touchedTabs: TouchedTabs,
): IssueSeverity | "none" | null {
  if (UNVALIDATED_TABS.includes(tabId) || !isTabTouched(touchedTabs, tabId)) return null;
  return ErrorChecker.getHighestSeverity(issueReport[tabId]) ?? "none";
}

/** Icon reflecting the highest severity of a tab, or a neutral circle for untouched tabs. */
function TabStatusIcon({ severity }: { severity: IssueSeverity | "none" | null }) {
  switch (severity) {
    case "error":
      return <CircleXIcon className="size-4 shrink-0 text-destructive" />;
    case "warning":
      return <CircleAlertIcon className="size-4 shrink-0 text-status-warning" />;
    case "info":
      return <InfoIcon className="size-4 shrink-0 text-status-info" />;
    case "none":
      return <CircleCheckIcon className="size-4 shrink-0 text-status-success" />;
    default:
      return <CircleIcon className="size-4 shrink-0 text-muted-foreground/30" />;
  }
}
