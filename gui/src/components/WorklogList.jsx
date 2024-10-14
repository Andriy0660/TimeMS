import Worklog from "./Worklog.jsx";
import {CircularProgress} from "@mui/material";
import NoLogs from "./NoLogs.jsx";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import useWorklogMutations from "../hooks/useWorklogMutations.js";

export default function WorklogList({worklogs, isWorklogsListing, isJiraEditMode}) {
  const {onCreateTimeLogFromWorklog} = useTimeLogMutations();
  const {onDelete} = useWorklogMutations();

  if (isWorklogsListing) {
    return <div className="text-center">
      <CircularProgress />
    </div>
  }

  return (
    <div className={`m-4`}>
      <div className="flex flex-col items-center">
        <div className="w-full overflow-x-auto">
          {worklogs.length
            ? worklogs.map(worklog =>
              <Worklog
                key={worklog.id}
                worklog={worklog}
                onDelete={onDelete}
                onTimeLogCreate={onCreateTimeLogFromWorklog}
                isJiraEditMode={isJiraEditMode}
              />)
            : <NoLogs />
          }
        </div>
      </div>
    </div>
  );
}