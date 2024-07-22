import {useContext} from "react";
import AppContext from "./AppContext.jsx";

export default function useAppContext() {
  return useContext(AppContext);
}
