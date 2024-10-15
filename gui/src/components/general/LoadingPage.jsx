import {CircularProgress} from "@mui/material";

export default function LoadingPage() {
  return (
    <div className="absolute inset-1/2">
      <CircularProgress />
    </div>
  )
}