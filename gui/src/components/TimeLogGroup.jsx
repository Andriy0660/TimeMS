import Divider from "@mui/material/Divider";

export default function TimeLogGroup({children, isJiraEditMode, isSynced, color}) {
  return (
    <div>
      <div
        className={`px-4 py-1 ${isJiraEditMode ? "shadow-md rounded-md mb-2" : "bg-gray-50"}`}
      >
        {children}
      </div>
      {!isJiraEditMode && <Divider />}
    </div>
  );
}