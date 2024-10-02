import {CircularProgress} from "@mui/material";
import Button from "@mui/material/Button";
import useWorklogSync from "../hooks/useWorklogSync.js";

export default function SyncWorklogsButton({children}) {
  const {syncWorklogs, progressInfo: {progress}, isSyncingRunning, isSyncingLaunched} = useWorklogSync();

  return (
    <Button onClick={syncWorklogs} className="mx-4 bg-white" sx={{color: "primary.main"}} disabled={isSyncingLaunched || progress > 0}
            variant="contained">
      {isSyncingRunning
        ? (
          <>
            {progress > 0 ? `${Math.floor(progress)}%` : <CircularProgress size={25} />}
            <CircularProgress className="ml-1" variant="determinate" size={25} value={progress} />
          </>
        )
        : <div>{children}</div>}
    </Button>
  )
}