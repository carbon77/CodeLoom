package com.codeloom.backend.dao.topic

import com.codeloom.backend.model.Topic
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TopicRepository : CrudRepository<Topic, UUID>, TopicRepositoryCustom
