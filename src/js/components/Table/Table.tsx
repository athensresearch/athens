import React from 'react';
import { Button, Box, Table, Thead, Tbody, Th, Td, Tr } from '@chakra-ui/react';


export const QueryTable = (props) => {
    const { data, columns } = props;
    return (columns && columns.length > 0 && <Table>
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
        )
}
