import Divider from "@mui/material/Divider";
const timeLogGroupPaddingX = 16;
export default function TimeLogGroup({children, isJiraEditMode, className}) {
  return (
    <div>
      <div
        className={`bg-gray-50 ${isJiraEditMode ? `shadow-md rounded-md ${className}` : ""}`}
        style={{padding: `4px ${timeLogGroupPaddingX}px 4px ${timeLogGroupPaddingX}px`}}
      >
        {children}
      </div>
      {!isJiraEditMode && <Divider />}
    </div>
  );
}
export {timeLogGroupPaddingX};