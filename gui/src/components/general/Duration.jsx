import {Chip} from "@mui/material";

export default function Duration({duration, className}) {
  return <Chip
    label={duration}
    color="primary"
    variant="outlined"
    size="small"
    className={`shadow-md ${className}`}
  />;

}