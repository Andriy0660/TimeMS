import {CircularProgress} from "@mui/material";
import Button from "@mui/material/Button";
import useAppContext from "../context/useAppContext.js";

export default function SyncWorklogsButton({children, className}) {
  const {syncWorklogs, isSyncingLaunched, isSyncingRunning, progressInfo: {progress}} = useAppContext();

  return (
    <Button onClick={syncWorklogs} className={`bg-white ${className}`} sx={{color: "primary.main"}} disabled={isSyncingLaunched || isSyncingRunning}
            variant="contained">
      <ButtonContent
        isSyncingLaunched={isSyncingLaunched}
        isSyncingRunning={isSyncingRunning}
        progress={progress}
        className="ml-1"
      >
        {children}
      </ButtonContent>
    </Button>
  )
}

function ButtonContent({
  children,
  isSyncingLaunched,
  isSyncingRunning,
  progress,
  className
}) {
  if (!isSyncingLaunched && !isSyncingRunning) {
    return children;
  }

  if (isSyncingLaunched && !isSyncingRunning) {
    return <CircularProgress size={25} />
  }

  return (
    <>
      {`${Math.round(progress)}%`}
      <CircularProgress className={className} variant="determinate" size={25} value={progress} />
    </>
  );
}