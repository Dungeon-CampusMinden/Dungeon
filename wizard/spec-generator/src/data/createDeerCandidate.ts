import type {
  AnyGraphNode,
  AnyResource,
  AnyRiddleInput,
  Asset,
  DeerSchema,
  Riddle,
} from "./DeerSchema";
import type { WizardDraft } from "./WizardDraft";

function projectGraphNode(node: AnyGraphNode): AnyGraphNode {
  if (node.kind === "start") return { id: node.id, kind: "start" };
  if (node.kind === "end") return { id: node.id, kind: "end", surfaceId: node.surfaceId };
  return { id: node.id, kind: "riddle", riddleId: node.riddleId };
}

function projectResource(resource: AnyResource): AnyResource {
  if (resource.kind === "inline_text") {
    return { id: resource.id, kind: "inline_text", title: resource.title, text: resource.text };
  }
  return { id: resource.id, kind: "asset", title: resource.title, assetId: resource.assetId };
}

function projectInput(input: AnyRiddleInput): AnyRiddleInput {
  if (input.type === "collection") {
    return { id: input.id, type: "collection", informationSourceId: input.informationSourceId };
  }
  return {
    id: input.id,
    type: "numeric",
    surfaceId: input.surfaceId,
    answer: input.answer,
    showDigitCount: input.showDigitCount,
  };
}

function projectRiddle(riddle: Riddle): Riddle {
  return {
    id: riddle.id,
    title: riddle.title,
    difficulty: riddle.difficulty,
    learningObjectiveIds: [...riddle.learningObjectiveIds],
    estimatedMinutes: riddle.estimatedMinutes,
    informationSources: riddle.informationSources.map((source) => ({
      id: source.id,
      surfaceId: source.surfaceId,
      resources: source.resources.map(projectResource),
    })),
    inputs: riddle.inputs.map(projectInput),
    hints: riddle.hints.map((hint) => ({
      id: hint.id,
      title: hint.title,
      text: hint.text,
      severity: hint.severity,
    })),
  };
}

function projectAsset(asset: Asset): Asset {
  return {
    id: asset.id,
    path: asset.path,
    mediaType: asset.mediaType,
    source: {
      license: asset.source.license,
      ...(asset.source.attribution?.trim() ? { attribution: asset.source.attribution } : {}),
    },
  };
}

/** Projects private draft state onto the exact public DEER candidate boundary. */
export function createDeerCandidate(draft: WizardDraft): DeerSchema {
  if (draft.seed === undefined) {
    throw new Error("Der Entwurf hat noch keinen Seed.");
  }
  const referencedAssetIds = new Set<string>();
  for (const riddle of draft.project.riddles) {
    for (const informationSource of riddle.informationSources) {
      for (const resource of informationSource.resources) {
        if (resource.kind === "asset") referencedAssetIds.add(resource.assetId);
      }
    }
  }

  const { project } = draft;
  return {
    formatVersion: "0.4",
    seed: draft.seed,
    metadata: {
      id: project.metadata.id,
      title: project.metadata.title,
      locale: project.metadata.locale,
      ...(project.metadata.description?.trim()
        ? { description: project.metadata.description }
        : {}),
      ...(project.metadata.author?.trim() ? { author: project.metadata.author } : {}),
    },
    learningDesign: {
      objectives: project.learningDesign.objectives.map((objective) => ({
        id: objective.id,
        description: objective.description,
      })),
      debriefPrompts: [...project.learningDesign.debriefPrompts],
    },
    session: {
      targetAudience: project.session.targetAudience,
      priorKnowledge: project.session.priorKnowledge,
      playerCount: {
        min: project.session.playerCount.min,
        max: project.session.playerCount.max,
      },
      time: {
        limitMinutes: project.session.time.limitMinutes,
        limitMode: project.session.time.limitMode,
      },
    },
    scenario: {
      themeId: project.scenario.themeId,
      mission: project.scenario.mission,
      introText: [...project.scenario.introText],
      successText: [...project.scenario.successText],
      ...(project.scenario.failureText?.length
        ? { failureText: [...project.scenario.failureText] }
        : {}),
    },
    surfaces: project.surfaces.map((surface) => ({
      id: surface.id,
      kind: surface.kind,
      title: surface.title,
    })),
    riddleGraph: {
      nodes: project.riddleGraph.nodes.map(projectGraphNode),
      edges: project.riddleGraph.edges.map((edge) => ({ from: edge.from, to: edge.to })),
    },
    riddles: project.riddles.map(projectRiddle),
    assets: project.assets
      .filter((asset) => referencedAssetIds.has(asset.id))
      .map(projectAsset),
  };
}
