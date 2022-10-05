import React, { useEffect } from "react";

import { Pagination, Stack } from "@mui/material";

import { useKanbanCardCreateContext } from "../../providers/kanban-card-create";

import { searchTagsByProjectIdAndPrefix } from "../../apis/tags-api";
import { useKanbanProjectsContext } from "../../providers/kanban-projects";

import KanbanTagArea from "../kanban-common/kanban-tags-area";
import { Tag } from "../../features/Tag";

const KanbanCardTagsSearchResultPanel = (props: any) => {
  // ------------------ Project ------------------
  const projectsContextState = useKanbanProjectsContext().state;

  // ------------------ Card create dialog ------------------
  const cardCreateContextState = useKanbanCardCreateContext().state;

  const [ tags, setTags ] = React.useState<Array<Tag>>([]);

  const [ page, setPage ] = React.useState(1);
  const [ totalPage, setTotalPage ] = React.useState(1); 

  const fetchTags = (page: number) => {
    const timeout = setTimeout(() => {  
      searchTagsByProjectIdAndPrefix(projectsContextState?._activeProject?.id, 
        cardCreateContextState._tagsEditAreaSearchStr, page).then(res => {
          setTags(res.tags);

          setPage(res.page + 1);
          setTotalPage(res.totalPage === 0? 1 : res.totalPage);
        });
    }, 1000);

    return () => clearTimeout(timeout);
  }

  useEffect(() => {
    fetchTags(0);
  }, [ cardCreateContextState._tagsEditAreaSearchStr]);

  const handleOnPageChange = (e: any, val: number) => {
    if(cardCreateContextState._lastFocusedArea === 'tagsEditArea') {
      fetchTags(val - 1);
    }
  }

  return (
    <section style={{ height: "100%" }}>
      <Stack 
        direction="column" 
        justifyContent="space-between"         
        style={{
          height: "100%",
          padding: "4px 0px 4px 4px"
          }}>
        <Stack                          
          direction="row"
          spacing={ 0.5 }
          style={{  
            flexWrap: "wrap",
            overflowY: "auto"}}>
          {
            tags.map(tag => {
            return (<KanbanTagArea tag={ tag.name } showDelete={ false } />)
            })
          }
        </Stack>

        <Pagination 
          color="primary"
          count={ totalPage } 
          page={ page } 
          onChange={ handleOnPageChange }
          style={{ paddingTop: "4px" }} />
      </Stack>
    </section>
  )
}

export default KanbanCardTagsSearchResultPanel;