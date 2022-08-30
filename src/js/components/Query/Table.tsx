import React from 'react';
import { Button, Box, Table, Thead, Tbody, Th, Td, Tr, Tfoot, textDecoration, Text, Link } from '@chakra-ui/react';
import { AddCardButton } from '../KanbanBoard/KanbanBoard'
import { ChevronDownIcon, ChevronUpIcon } from '@/Icons/Icons';


const isDateFn = (value) => {
    return (typeof value == "number") && (value > 1000000000000)
}

const cellValue = (column, record, hideProperties, dateFormatFn, onUidClick, onPageClick) => {
    const value = record[column];
    const isDate = isDateFn(value)
    const uid = record[":block/uid"]
    if (hideProperties[column]) {
        return
    }
     else if (isDate) {
        return dateFormatFn(value)
    }
    else if (column == ":task/page") {
        return <Link onClick={() => onPageClick(value)} color="link">{value}</Link>
    }
    else if (column == ":block/uid") {
        return <Link onClick={() => onUidClick(uid)} color="highlight">{value}</Link>
    }
    else {
        return value
    }
}


export const QueryTable = (props) => {
    const { data, columns, dateFormatFn, onClickSort, sortBy, sortDirection, hideProperties, rowCount, onUidClick, onPageClick} = props;
    return (columns && columns.length > 0 && <Box>
        <Table>
            <Thead>
                {/* Column headers control sorting */}
                {/* <Tr>{columns.map((column) =>
                    (!hideProperties[column] &&
                        <Th key={column} onClick={()=>onClickSort(column)}>
                            <Button>
                                {column}
                                {(sortBy == column &&
                                    (sortDirection == "asc" ? <ChevronUpIcon/> : <ChevronDownIcon/>))}
                            </Button>
                        </Th>))}
                </Tr> */}
                <Tr>{columns.map((column => 
                        <Th key={column} textDecoration={sortBy==column && "underline"}>
                            {column}
                            {(sortBy == column &&
                                (sortDirection == "asc" ? <ChevronUpIcon ml="1"/> : <ChevronDownIcon ml="1" fontWeight={""}/>))}
                        </Th>))}
                </Tr>
            </Thead>
            <Tbody>
            {data.map((record) => {
                return <Tr>
                    {columns.map((column) => {
                        return <Td>{cellValue(column, record, hideProperties, dateFormatFn, onUidClick, onPageClick)}</Td>
                    })}
                </Tr>})}
            </Tbody>
            <Tfoot>
                <Tr>
                    <Th>
                        Count: {rowCount}
                    </Th>
                </Tr>
            </Tfoot>
        </Table>
        <Button display="flex" width="100%" onClick={()=>console.log("TODO: onAddNewCardClick on table view.")}>
            + New
        </Button>
        {/* <AddCardButton column={null} onAddNewCardClick={null} /> */}
    </Box>)
}
