import {useEffect, useRef, useState} from "react";
import {startHourOfDay} from "../config/timeConfig.js";
import {useQuery} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import dateTimeService from "../service/dateTimeService.js";
import timeLogProcessingService from "../service/timeLogProcessingService.js";
import useAppContext from "../context/useAppContext.js";

export default function useProcessedTimeLogs() {
  const queryParams = new URLSearchParams(location.search);
  const {date, addAlert, mode} = useAppContext();
  const [timeLogs, setTimeLogs] = useState([]);
  const [totalTimeLabel, setTotalTimeLabel] = useState("")

  const offset = startHourOfDay;
  const [groupByDescription, setGroupByDescription] = useState(!!queryParams.get("groupByDescription") || false);
  const [filterTickets, setFilterTickets] = useState([""])
  const [selectedTickets, setSelectedTickets] = useState([]);

  const {
    data,
    isPending: isListing,
    error: listAllError
  } = useQuery({
    queryKey: [timeLogApi.key, mode, date, offset],
    queryFn: () => {
      return timeLogApi.list(dateTimeService.calculateDateRange(mode, date));
    },
    retryDelay: 300,
  });

  useEffect(() => {
    if (listAllError) {
      addAlert({
        text: `${listAllError.displayMessage} Try agail later`,
        type: "error"
      });
    }
  }, [listAllError]);

  const processedDataRef = useRef([]);
  useEffect(() => {
    const processedData = timeLogProcessingService.processData(data, selectedTickets);
    processedDataRef.current = processedData;

    const filterTickets = getFilterTickets(data);
    updateSelectedTicketsIfNeeded(filterTickets);

    const groupedData = groupAndSortData(processedData, groupByDescription);
    const label = calculateTotalTimeLabel(groupedData, groupByDescription);
    setTimeLogs(groupedData)
    setTotalTimeLabel(label);
  }, [data, groupByDescription, selectedTickets])

  function getFilterTickets(data) {
    const filterTickets = timeLogProcessingService.extractTickets(data);
    filterTickets.push("Without ticket");
    setFilterTickets(filterTickets);
    return filterTickets;
  }

  function updateSelectedTicketsIfNeeded(filterTickets) {
    const updatedTickets = selectedTickets.filter(ticket => filterTickets.includes(ticket));
    if (selectedTickets.toString() !== updatedTickets.toString()) {
      setSelectedTickets(updatedTickets);
    }
  }

  function groupAndSortData(data, groupByDescription) {
    if (groupByDescription) {
      return timeLogProcessingService.group(data, ["date", "ticketAndDescription"]);
    } else {
      return timeLogProcessingService.group(data, ["date"]);
    }
  }

  function calculateTotalTimeLabel(groupedData, groupByDescription) {
    if (groupByDescription) {
      return dateTimeService.formatDuration(dateTimeService.getTotalTimeGroupedByDateAndDescription(groupedData.data));
    } else {
      return dateTimeService.formatDuration(dateTimeService.getTotalTimeGroupedByDate(groupedData.data));
    }
  }
  return {
    groupByDescription,
    setGroupByDescription,
    timeLogs,
    processedTimeLogsArray: processedDataRef.current,
    isListing,
    totalTimeLabel,
    filterTickets,
    selectedTickets,
    setSelectedTickets,
  }
}