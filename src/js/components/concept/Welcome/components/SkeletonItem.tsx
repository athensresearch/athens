import React from "react";
import styled from "styled-components";

import { Button } from "@/Button";

const ItemWrap = styled.li`
  align-self: stretch;
  display: flex;
  align-items: stretch;
  justify-content: stretch;
  display: grid;
  grid-template-areas: "main";

  &:nth-child(2) {
    opacity: 0.8;
  }
  &:nth-child(3) {
    opacity: 0.6;
  }
  &:nth-child(4) {
    opacity: 0.4;
  }
  &:nth-child(5) {
    opacity: 0.2;
  }
  &:nth-child(6) {
    opacity: 0.1;
  }
`;

const ItemButton = styled(Button)`
  grid-area: main;
  flex-direction: column;
  align-items: stretch;
  gap: 0;
  flex: 1 1 100%;
  display: grid;
  justify-items: stretch;
  text-align: left;
  grid-template-areas: "icon name status" "icon detail status";
  grid-template-columns: auto 1fr;
  gap: 0 0.125rem;
  border-radius: 0.5rem;
  position: relative;
  z-index: 1;
  padding-right: 4rem;
  background: var(--background-plus-1---opacity-med);
`;

const Name = styled.h3`
  margin: 0;
  display: flex;
  grid-area: name;
  justify-self: stretch;
  width: 50%;
  height: .9rem;
  border-radius: 0.25rem;
  background: var(--background-plus-2---opacity-med);
`;

const Detail = styled.span`
  grid-area: detail;
  width: 100%;
  height: .9rem;
  align-self: flex-end;
  border-radius: 0.25rem;
  background: var(--background-plus-2---opacity-med);
`;

const Icon = styled.div`
  width: 2em;
  height: 2em;
  border-radius: 18%;
  background: var(--background-plus-2---opacity-med);
  grid-area: icon;
  grid-row: 1 / -1;
  margin: auto;
`;

interface SkeletonItemProps { }

export const SkeletonItem = (props: SkeletonItemProps): React.ReactElement => {
  return (
    <>
      <ItemWrap>
        <ItemButton as="span">
          <Icon />
          <Name></Name>
          <Detail></Detail>
          {/* TODO: Tooltip or help text when trying to interact with an offline DB */}
        </ItemButton>
      </ItemWrap>
    </>
  );
};
