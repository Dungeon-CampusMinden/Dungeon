import { Tabs, TabsList, TabsTrigger } from "./ui/tabs";
import { CircleAlertIcon, CircleCheckIcon, CircleIcon, CircleXIcon, InfoIcon } from "lucide-react";
import { ErrorChecker, type IssueReport, type IssueSeverity } from "@/data/ErrorChecker";
import { isTabTouched, type TouchedTabs } from "@/data/TabTouchState";
import { TABS } from "@/data/Tabs";

/** Tabs that only present content and are therefore never validated. */
const UNVALIDATED_TABS = ["review"];

export function SidebarNavigation({
  issueReport,
  touchedTabs,
  tab,
  setTab,
  className,
}: {
  issueReport: IssueReport;
  touchedTabs: TouchedTabs;
  tab: string;
  setTab: (tab: string) => void;
  className?: string;
}) {
  return (
    <div className={`panel ${className ?? ""} flex flex-col gap-0`}>
      <h2>Outline</h2>
      <Tabs value={tab} onValueChange={(value) => setTab(value)} orientation="vertical" className="mt-0">
        <TabsList className="bg-transparent">
          {TABS.map((entry) => (
            <TabsTrigger key={entry.value} value={entry.value}>
              <TabStatusIcon severity={getTabSeverity(entry.value, issueReport, touchedTabs)} />
              {entry.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>
    </div>
  );
}

/** Feedback is only shown once the user has actually worked on a tab. */
function getTabSeverity(
  tabId: string,
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
      return <CircleXIcon className="text-red-500" />;
    case "warning":
      return <CircleAlertIcon className="text-yellow-500" />;
    case "info":
      return <InfoIcon className="text-blue-500" />;
    case "none":
      return <CircleCheckIcon className="text-green-500" />;
    default:
      return <CircleIcon className="text-muted-foreground" />;
  }
}
