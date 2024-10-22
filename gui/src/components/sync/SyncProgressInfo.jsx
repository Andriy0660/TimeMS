import {useState} from "react";

export default function SyncProgressInfo({children, className}) {

  const [isHovered, setHovered] = useState(false);
  return (
    <div
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      className={`${isHovered ? "font-bold" : ""} p-2 bg-gray-100 rounded-xl shadow-md ${className}`}>{children}</div>
  )
}