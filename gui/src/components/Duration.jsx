import {Chip} from "@mui/material";

export default function Duration({duration}) {
  return <Chip
    label={duration}
    color="primary"
    variant="outlined"
    size="small"
    className="shadow-md mx-2"
  />;

}