import {useEffect, useState} from "react";

export default function Connector({startElement, endElement, color}) {
  const [positions, setPositions] = useState({startX: 0, startY: 0, endX: 0, endY: 0});

  const updatePositions = () => {
    if (startElement && endElement) {
      const startRect = startElement.getBoundingClientRect();
      const endRect = endElement.getBoundingClientRect();

      const startX = startRect.right;
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
      />
    </svg>
  );
}
