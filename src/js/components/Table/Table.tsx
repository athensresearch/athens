import React from 'react';
import { Button, Box, Table, Thead, Tbody, Th, Td, Tr } from '@chakra-ui/react';


export const QueryTable = (props) => {
    const { data, columns } = props;
    console.log(columns)
    return <Table>
        <Thead>
        <Tr>{columns.map((column) =>
            <Th key={column}>{column}</Th>)}</Tr>
        </Thead>
        <Tbody>
        {data.map(({id, title, type, status, project, assignee}) =>
            <Tr>
            <Td>{id}</Td>
            <Td>{title}</Td>
            <Td>{type}</Td>
            <Td>{status}</Td>
            <Td>{project}</Td>
            <Td>{assignee}</Td>
            </Tr>
            )}
    </Tbody>
        </Table>
}
