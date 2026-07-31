import type { DeerSchema } from "@/data/DeerSchema";
import { Tabs, TabsList, TabsTrigger } from "./ui/tabs";
import { CircleAlertIcon, CircleCheckIcon, CircleIcon, CircleXIcon } from "lucide-react";

export function SidebarNavigation({
  deerSchema,
  updateDeerSchema,
  tab,
  setTab,
  className,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
  tab: string;
  setTab: (tab: string) => void;
  className?: string;
}) {
  return (
    <div className={`panel ${className ?? ""} flex flex-col gap-0`}>
      <h2>Outline</h2>
      <Tabs value={tab} onValueChange={(value) => setTab(value)} orientation="vertical" className="mt-0">
        <TabsList className="bg-transparent">
          <TabsTrigger value="metadata">
            <CircleCheckIcon className="text-green-500" />
            Eckdaten & Lernziel
          </TabsTrigger>
          <TabsTrigger value="scenario">
            <CircleCheckIcon className="text-green-500" />
            Geschichte
          </TabsTrigger>
          <TabsTrigger value="session">
            <CircleAlertIcon className="text-yellow-500" />
            Spieleinstellungen
          </TabsTrigger>
          <TabsTrigger value="surfaces">
            <CircleCheckIcon className="text-green-500" />
            Orte
          </TabsTrigger>
          <TabsTrigger value="assets">
            <CircleAlertIcon className="text-yellow-500" />
            Eigene Bilder & Dateien
          </TabsTrigger>
          <TabsTrigger value="riddles">
            <CircleXIcon className="text-red-500" />
            Rätsel
          </TabsTrigger>
          <TabsTrigger value="riddle_graph">
            <CircleXIcon className="text-red-500" />
            Spielablauf
          </TabsTrigger>
          <TabsTrigger value="review">
            <CircleIcon />
            Prüfen & Vorschau
          </TabsTrigger>
        </TabsList>
      </Tabs>
    </div>
  );
}
