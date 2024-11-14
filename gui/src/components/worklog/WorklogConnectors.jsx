import {syncStatus} from "../../consts/syncStatus.js";
import Connector from "../general/Connector.jsx";

export default function WorklogConnectors ({
  isHovered,
  sourceRefs,
  targetRefs,
  sourceItem
}) {
  if (!isHovered) return null;
  if (!sourceRefs?.length || !targetRefs?.length) return null;

  const targetColor = sourceItem.jiraSyncInfo.color;
  const isDashed = sourceItem.jiraSyncInfo.status === syncStatus.PARTIAL_SYNCED;

  return (
    <>
      {sourceRefs.map((sourceRef, index1) => {
        return targetRefs.map((targetRef, index2) => {
          const sourceColor = sourceRef.timeLog?.jiraSyncInfo?.color ||
            sourceRef.externalTimeLog?.jiraSyncInfo?.color;
          const targetColor2 = targetRef.timeLog?.jiraSyncInfo?.color ||
            targetRef.externalTimeLog?.jiraSyncInfo?.color;

          if (sourceColor === targetColor && targetColor2 === targetColor) {
            return (
              <Connector
                key={`${index1}${index2}`}
                startElement={sourceRef.ref.current}
                endElement={targetRef.ref.current}
                color={targetColor}
                dashed={isDashed}
              />
            );
          }
          return null;
        });
      })}
    </>
  );
}

