import {CircularProgress} from "@mui/material";
import Button from "@mui/material/Button";
import useAppContext from "../context/useAppContext.js";

export default function SyncWorklogsButton({children}) {
  const {syncWorklogs, isSyncingLaunched, isSyncingRunning, progressInfo: {progress}} = useAppContext();

  return (
    <Button onClick={syncWorklogs} className="mx-4 bg-white" sx={{color: "primary.main"}} disabled={isSyncingLaunched}
            variant="contained">
      {!isSyncingLaunched && <>{children}</>}
      {isSyncingLaunched && !isSyncingRunning && <CircularProgress size={25} />}
      {isSyncingLaunched && isSyncingRunning && (
        <>
          {`${Math.round(progress)}%`}
          <CircularProgress className="ml-1" variant="determinate" size={25} value={progress} />
        </>
      )}
    </Button>
  )
}