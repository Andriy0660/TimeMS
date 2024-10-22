import {syncStatus} from "../../consts/syncStatus.js";
import {useEffect, useState} from "react";
import {timeLogGroupPaddingX} from "./TimeLogGroup.jsx";

export default function TimeLogWorklogConnectors ({
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
            sourceRef.worklog?.jiraSyncInfo?.color;
          const targetColor2 = targetRef.timeLog?.jiraSyncInfo?.color ||
            targetRef.worklog?.jiraSyncInfo?.color;

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

function Connector({startElement, endElement, color, dashed}) {
  const [positions, setPositions] = useState({startX: 0, startY: 0, endX: 0, endY: 0});
  const updatePositions = () => {
    if (startElement && endElement) {
      const startRect = startElement.getBoundingClientRect();
      const endRect = endElement.getBoundingClientRect();

      const startX = startRect.right + timeLogGroupPaddingX;
      const startY = startRect.top + startRect.height / 2 + window.scrollY;
      const endX = endRect.left;
      const endY = endRect.top + endRect.height / 2 + window.scrollY;

      setPositions({startX, startY, endX, endY});
    }
  };

  useEffect(() => {
    updatePositions();
    window.addEventListener("scroll", updatePositions);
    return () => window.removeEventListener("scroll", updatePositions);
  }, [startElement, endElement]);

  const documentHeight = document.documentElement.scrollHeight;
  return (
    <svg className="absolute top-0 left-0 w-full pointer-events-none"
         style={{height: documentHeight}}
    >
      <line
        x1={positions.startX}
        y1={positions.startY}
        x2={positions.endX}
        y2={positions.endY}
        stroke={color}
        strokeWidth="2"
        strokeDasharray={dashed ? "8 4" : "none"}
      />
    </svg>
  );
}
