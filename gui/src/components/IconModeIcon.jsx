import {IconButton, Tooltip} from '@mui/material';

export default function IconModeIcon({title, icon, isActive, onClick}) {
  const activeIconClasses = "border-blue-400 border-solid border rounded";

  return (
    <Tooltip title={title}>
      <IconButton
        onClick={onClick}
        className={isActive ? activeIconClasses : ""}
      >
        {icon}
      </IconButton>
    </Tooltip>
  );
}