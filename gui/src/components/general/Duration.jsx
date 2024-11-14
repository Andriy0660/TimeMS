import {Chip} from "@mui/material";

export default function Duration({duration, className, color="blue"}) {
  const colorClasses = {
    blue: "text-blue-500 border-blue-500",
    green: "text-green-500 border-green-500",
  };

  return <Chip
    label={duration}
    variant="outlined"
    size="small"
    className={`shadow-md ${colorClasses[color]} ${className}`}
  />;

}