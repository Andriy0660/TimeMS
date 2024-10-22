import Divider from "@mui/material/Divider";

export default function VerticalDivider({className}) {
  return <Divider className={`bg-gray-500 ${className}`} orientation="vertical" variant="middle" sx={{borderRightWidth: 2}} flexItem />
}