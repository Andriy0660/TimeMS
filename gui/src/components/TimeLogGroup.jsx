import Divider from "@mui/material/Divider";
const timeLogGroupPaddingX = 16;
export default function TimeLogGroup({children, isJiraEditMode}) {
  return (
    <div>
      <div
        className={`bg-gray-50 ${isJiraEditMode ? "shadow-md rounded-md mb-2" : ""}`}
        style={{padding: `4px ${timeLogGroupPaddingX}px 4px ${timeLogGroupPaddingX}px`}}
      >
        {children}
      </div>
      {!isJiraEditMode && <Divider />}
    </div>
  );
}
export {timeLogGroupPaddingX};