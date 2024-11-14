import Divider from "@mui/material/Divider";
const timeLogGroupPaddingX = 16;
export default function TimeLogGroup({children, isInEditMode, className}) {
  return (
    <div>
      <div
        className={`bg-gray-50 ${isInEditMode ? `shadow-md rounded-md ${className}` : ""}`}
        style={{padding: `4px ${timeLogGroupPaddingX}px 4px ${timeLogGroupPaddingX}px`}}
      >
        {children}
      </div>
      {!isInEditMode && <Divider />}
    </div>
  );
}
export {timeLogGroupPaddingX};